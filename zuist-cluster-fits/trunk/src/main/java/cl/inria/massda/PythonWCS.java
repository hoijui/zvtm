
package cl.inria.massda;

import java.io.IOException;
//import java.util.concurrent.locks.Lock;

import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Channel;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.geom.Point2D;

import cl.inria.massda.SmartiesManager.MyCursor;


public class PythonWCS {

    Producer producer;
    //Point2D.Double coordinate;
    //String galactic = "";


    static String PRODUCER_ROUTINGKEY = "java";
    static String CONSUMER_ROUTINGKEY = "python";
    static String HOST_NAME = "192.168.1.213";
    static String VIRTUAL_HOST = "/";
    static String USER_ID = "guest";
    static String PASSWORD = "guest";
    static String CONSUMER_TAG = "consumerTag";

    MyCursor myCursor;

    boolean galacticSystem = false;


    public class LocalConsumer extends DefaultConsumer{
        Channel channel;
        public LocalConsumer(Channel ch){
            super(ch);
            channel = ch;
        }
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, 
                BasicProperties properties, byte[] body) throws IOException{
            String routingKey = envelope.getRoutingKey();
            String contentType = properties.getContentType();
            long deliveryTag = envelope.getDeliveryTag();

            String str = new String(body, "UTF-8");
            System.out.println("Consumer " + CONSUMER_ROUTINGKEY);
            System.out.println(str);

            try{
                JSONObject json = new JSONObject(str);
                String sexagesimal;
                String coordinate;
                if(!galacticSystem){
                    sexagesimal = "Ecuatorial: " + json.getString("ecuatorial");
                    coordinate = "Ra: " + json.getDouble("x") + " - Dec: " + json.getDouble("y");
                } else {
                    sexagesimal = "Galactic: " + json.getString("galactic");
                    coordinate = "Lat: " + json.getDouble("lat") + " - Lon: " + json.getDouble("lon");
                }
                 
                
                //notify();
                if(myCursor != null){
                    myCursor.updateLabel(coordinate, sexagesimal);
                } else {
                    System.out.println("cursor null");
                }

            } catch (JSONException e){
                e.printStackTrace(System.out);
            }

            channel.basicAck(deliveryTag, false);
        }

    }

    public PythonWCS(){
        try{

            producer = new Producer(PRODUCER_ROUTINGKEY, HOST_NAME, VIRTUAL_HOST, USER_ID, PASSWORD);

            Consumer c = new Consumer(HOST_NAME, VIRTUAL_HOST, USER_ID, PASSWORD);
            c.declareExchange(CONSUMER_ROUTINGKEY);
            c.declareQueue(CONSUMER_ROUTINGKEY + "s", CONSUMER_ROUTINGKEY);

            Channel ch = c.getChannel();

            c.startConsuming(CONSUMER_TAG, new LocalConsumer(ch));

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    /*
    public Point2D.Double getCoordinate(){
        return coordinate;
    }

    public String getGalactic(){
        return galactic;
    }
    */

    public void changeCoordinateSystem(boolean galactic){
        galacticSystem = galactic;
    }

    public void sendCoordinate(double x, double y, MyCursor mc){

        //synchronized(this) {
            try{

                myCursor = mc;
            
                JSONObject json = new JSONObject();
                json.put("name", "pix2world");
                json.put("x", x);
                json.put("y", y);

                producer.publish(json.toString(), PRODUCER_ROUTINGKEY);
                System.out.println("Message " + json.toString() + " sent.");

                //wait();
            
            /*
            } catch (InterruptedException e){

                e.printStackTrace(System.out);
                System.out.println("Interrupted");
            */
            } catch (JSONException e){
                e.printStackTrace(System.out);
                System.out.println("Error send a JSON");
            } catch (IOException e){
                e.printStackTrace(System.out);
                System.out.println("Error IO");
            }
        //}
        
    }


}

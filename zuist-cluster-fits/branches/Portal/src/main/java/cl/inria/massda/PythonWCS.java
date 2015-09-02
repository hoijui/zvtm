
package cl.inria.massda;

import java.io.IOException;
//import java.util.concurrent.locks.Lock;

import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Channel;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.awt.geom.Point2D;

import cl.inria.massda.SmartiesManager.MyCursor;

import java.util.Observer;
import java.util.Observable;

import fr.inria.zuist.viewer.Options;

public class PythonWCS extends Observable{

    /*
    static String PRODUCER_ROUTINGKEY = "java";
    static String CONSUMER_ROUTINGKEY = "python";
    static String HOST_NAME = "192.168.1.213";
    static String VIRTUAL_HOST = "/";
    static String USER_ID = "guest";
    static String PASSWORD = "guest";
    static String CONSUMER_TAG = "consumer";
    */

    Producer producer;


    //MyCursor myCursor;

    //boolean galacticSystem = false;

    //public Vector<Observer> ;


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
            System.out.println("Consumer: " + Options.CONSUMER_ROUTINGKEY_MQ);
            System.out.println(str);

            try{
                JSONObject json = new JSONObject(str);
                notifyObs(json);

            } catch (JSONException e){
                e.printStackTrace(System.out);
            }

            channel.basicAck(deliveryTag, false);
        }

    }

    /*
    @Override
    public void notifyObservers(Object arg){
        System.out.println("notifyObservers(Object arg)");
    }
    */

    
    public void notifyObs(JSONObject json){
        System.out.println("--- notifyObs ---");
        try{
            System.out.println(json.getString("id"));
        } catch (JSONException e){
            e.printStackTrace(System.out);
        }
        
        System.out.println("hasChanged(): " +  hasChanged() );
        setChanged();
        System.out.println("hasChanged(): " +  hasChanged() );
        System.out.println("setChanged()");

        System.out.println("countObservers(): " + countObservers());
        notifyObservers(json);
        System.out.println("countObservers(): " + countObservers());
        System.out.println("notifyObservers(json)");

    }
    

    public PythonWCS(){
        try{

            producer = new Producer(Options.PRODUCER_ROUTINGKEY_MQ, Options.HOST_NAME_MQ, Options.VIRTUAL_HOST_MQ, Options.USER_ID_MQ, Options.PASSWORD_MQ);
            producer.declareQueue(Options.PRODUCER_ROUTINGKEY_MQ+"s", Options.PRODUCER_ROUTINGKEY_MQ);

            Consumer c = new Consumer(Options.HOST_NAME_MQ, Options.VIRTUAL_HOST_MQ, Options.USER_ID_MQ, Options.PASSWORD_MQ);
            c.declareExchange(Options.CONSUMER_ROUTINGKEY_MQ);
            c.declareQueue(Options.CONSUMER_ROUTINGKEY_MQ + "s", Options.CONSUMER_ROUTINGKEY_MQ);

            Channel ch = c.getChannel();

            c.startConsuming(Options.CONSUMER_TAG_MQ, new LocalConsumer(ch));

            System.out.println("new PythonWCS() -- countObservers(): " + countObservers());

        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    /*
    public void changeCoordinateSystem(boolean galactic){
        galacticSystem = galactic;
    }
    */


    public void sendPix2World(double x, double y, String id){

        //synchronized(this) {
            try{
            
                JSONObject json = new JSONObject();
                json.put("id", id);
                json.put("name", "pix2world");
                json.put("x", x);
                json.put("y", y);

                System.out.println("wcs.countObservers(): " + countObservers());
                //addObserver(obs);
                System.out.println("wcs.countObservers(): " + countObservers());

                if(producer != null)
                    producer.publish(json.toString(), Options.PRODUCER_ROUTINGKEY_MQ);
                else
                    System.out.println("Problem with the connection to RabbitMQ");
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

    public void sendWorld2Pix(double ra, double dec, String id){

        //synchronized(this) {
            try{
            
                JSONObject json = new JSONObject();
                json.put("id", id);
                json.put("name", "world2pix");
                json.put("ra", ra);
                json.put("dec", dec);

                //System.out.println("wcs.countObservers(): " + countObservers());
                //addObserver(obs);
                System.out.println("wcs.countObservers(): " + countObservers());


                if(producer != null)
                    producer.publish(json.toString(), Options.PRODUCER_ROUTINGKEY_MQ);
                else
                    System.out.println("Problem with the connection to RabbitMQ");
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

    /*
    public void sendWorld2Pix(double[] ra, double[] dec, String[] id){

        //synchronized(this) {
            try{
            
                JSONObject json = new JSONObject();
                
                json.put("name", "world2pix");
                JSONArray arr = new JSONArray();
                for(int i = 0; i < ra.length; i++){
                    JSONObject coor = new JSONObject();
                    coor.put("id", id[i]);
                    coor.put("ra", ra[i]);
                    coor.put("dec", dec[i]);
                    arr.put(coor);
                }
                json.put("coords", arr);
                System.out.println("wcs.countObservers(): " + countObservers());
                //addObserver(obs);
                //System.out.println("wcs.countObservers(): " + countObservers());

                producer.publish(json.toString(), PRODUCER_ROUTINGKEY);
                System.out.println("Message " + json.toString() + " sent.");

                //wait();
            
            /*
            } catch (InterruptedException e){

                e.printStackTrace(System.out);
                System.out.println("Interrupted");
            *
            } catch (JSONException e){
                e.printStackTrace(System.out);
                System.out.println("Error send a JSON");
            } catch (IOException e){
                e.printStackTrace(System.out);
                System.out.println("Error IO");
            }
        //}
        
    }
    */

    public void setReference(String src_path){

        try{
        
            JSONObject json = new JSONObject();
            json.put("name", "set_reference");
            json.put("src_path", src_path);

            if(producer != null)
                producer.publish(json.toString(), Options.PRODUCER_ROUTINGKEY_MQ);
            else
                System.out.println("Problem with the connection to RabbitMQ");
            System.out.println("Message " + json.toString() + " sent.");

        } catch (JSONException e){
            e.printStackTrace(System.out);
            System.out.println("Error send a JSON");
        } catch (IOException e){
            e.printStackTrace(System.out);
            System.out.println("Error IO");
        }
        
    }


}

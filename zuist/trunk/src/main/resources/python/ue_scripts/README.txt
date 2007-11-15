--------------------------------------------------------------------------------
Generating PNG bitmaps for each PDF page
./pdf2pngpages.py uist uist4z/papers

--------------------------------------------------------------------------------
Generating metadata:

./metadataProcessor.py uist4z metadata.xsl
./cdataRemover.py uist4z
./metadataMerger.py uist4z authorids.csv coauthors.csv authors.csv nkw1-keywords.csv nkw2-keywords.csv nkw3-keywords.csv videos.tsv
./prettym.sh

Eventually creates uist4z/UISTmetadata.xml

--------------------------------------------------------------------------------
Generating ZUIST scene:

./uist_scene_generator.py uist4z
./pretty.sh

Eventually creates uist4z/scene.xml

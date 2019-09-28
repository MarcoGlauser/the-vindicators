# Food Information Crawler
Searches leshop.ch for country of origin for a product, calculates the distance the product travelled 
through wolframalpha. Finds out whether the product is local and in season.
In the end it saves the data on Firebase.

# How to run crawler
* create secrets.py
* Open http://leshop.ch in a browser
* Copy the header "leshopch" from an XHR request
* Define the variable `leshop_token = 'TOKEN'` in secrets.py
* Define the variable `wolfram_app_id = 'APP_ID'` in secrets.py 
* Run `gcloud auth application-default login` to connect to firestore
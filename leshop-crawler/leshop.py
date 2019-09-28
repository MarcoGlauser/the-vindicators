import random
import re
import time

import requests

import secrets

s = requests.Session()

search_url = 'https://www.leshop.ch/supermarket/public/v3/api/search/languages/de/warehouses/2/products?query={search_query}&sortOrder=asc&categoryId=3823826'
detail_url = 'https://www.leshop.ch/supermarket/public/v4/api/productmetadata/language/de/products?id={product_id}'

headers = {
    'Cookie': '__cfduid=d0da560fb33cead1ba7b877341882bc921569623823; 7548b0db45f10d6fc4a301e6050516ea=2684f684cec900262494602835bec396',
    'leshopch': secrets.leshop_token,
    'User-Agent': 'Mozilla/5.0 (X11; Ubuntu Hack Zurich; Linux x86_64; rv:69.0) Gecko/20100101 Firefox/69.0'
}


def get_origin(product):
    product_id = get_first_product_id(product)
    sleep_time = random.random()
    time.sleep(sleep_time)
    origin = get_product_origin(product_id)
    cleaned_origin = clean_origin(origin)
    return cleaned_origin


def clean_origin(origin):
    m = re.search('[A-Za-zäöü]+', origin)
    return m.group()


def get_product_origin(product_id):
    response = s.get(url=detail_url.format(product_id=product_id), headers=headers)
    response.raise_for_status()
    return response.json()[0]['origin']


def get_first_product_id(search_query):
    response = s.get(url=search_url.format(search_query=search_query), headers=headers)
    response.raise_for_status()
    first_product = response.json()["productIds"][0]
    return first_product
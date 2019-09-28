import config
import leshop
import wolfram
import firestore

def main():
    for product_en, product_de in config.products:
        try:
            origin = leshop.get_origin(product_de)
            distance = wolfram.query_distance(origin)
            continent = wolfram.query_continent(origin)
            print(product_de)
            print(origin)
            print(distance)
            print(continent)
            firestore.save_product(product_en,
                                   name_de=product_de,
                                   name_en=product_en,
                                   country=origin,
                                   continent=continent,
                                   distance=distance
                                   )
        except:
            print(f'product {product_de} failed to crawl')
        print('')

main()
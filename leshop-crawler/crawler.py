import co2
import config
import leshop
import seasonality
import wolfram
import firestore
import transport


def main():
    for product_en, product_de in config.products:
        try:
            origin = leshop.get_origin(product_de)
            distance = wolfram.query_distance(origin)
            continent = wolfram.query_continent(origin)
            transport_mode = transport.get_transport_mode(product_en, origin, continent)
            in_season = seasonality.is_in_season(product_en)
            co2_emissions_per_kg = co2.get_co2_emission_per_product(origin, continent, transport_mode, in_season)

            firestore.save_product(product_en,
                                   name_de=product_de,
                                   name_en=product_en,
                                   country=origin,
                                   continent=continent,
                                   distance=distance,
                                   transport_mode=transport_mode,
                                   in_season=in_season,
                                   co2_emissions_per_kg=co2_emissions_per_kg
                                   )
            print(f'SUCCESS: {product_de}')
        except:
            print(f'SKIPPED: product {product_de} failed to crawl')
        print('')

main()
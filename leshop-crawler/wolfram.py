import re

import wolframalpha
import secrets

wolframalpha_client = wolframalpha.Client(secrets.wolfram_app_id)
wolframalpha_distance_query = 'Distance from {origin} to Zurich in kilometers'
wolframalpha_continent_query = 'On which continent is {origin}?'


def query_distance(origin):
    res = wolframalpha_client.query(wolframalpha_distance_query.format(origin=origin))
    distance = next(res.results).text
    return clean_distance(distance)


def clean_distance(distance):
    m = re.search('\d+', distance)
    distance = m.group()
    return int(distance)


def query_continent(origin):
    res = wolframalpha_client.query(wolframalpha_continent_query.format(origin=origin))
    return next(res.results).text
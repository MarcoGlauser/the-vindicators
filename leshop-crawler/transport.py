from enum import Enum


class TransportMode(Enum):
    PLANE = 'plane'
    SHIP = 'ship'
    LOCAL = 'local'
    TRUCK = 'truck'


plane_product_combinations = [
    ('mango', 'brasil'),
    ('mango', 'thailand'),
    ('mango', 'pakistan'),
    ('bean', 'ägypten'),
    ('bean', 'kenia'),
    ('bean', 'thailand'),
    ('aspargus', 'peru'),
    ('strawberry', 'ägypten'),
]

always_plane_product = [
    'papaya'
]


def get_transport_mode(product, origin, continent):
    if origin == 'Schweiz':
        return TransportMode.LOCAL.value
    elif continent == 'Europe':
        return  TransportMode.TRUCK.value
    elif product.lower() in always_plane_product:
        return TransportMode.PLANE.value
    elif (product.lower(), origin.lower()) in plane_product_combinations:
        return TransportMode.PLANE.value
    else:
        return TransportMode.SHIP.value
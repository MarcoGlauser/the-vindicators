from enum import Enum

import transport


class EmissionValues(Enum):
    REGIONAL_IN_SEASON = 530
    REGIONAL_NOT_IN_SEASON = REGIONAL_IN_SEASON * 5
    EUROPE = 760
    OVERSEAS_SHIP = 870
    OVERSEAS_PLANE = 11300


def get_co2_emission_per_product(origin, continent, transport_type, in_season):
    if origin == "Schweiz" and in_season:
        return EmissionValues.REGIONAL_IN_SEASON.value
    elif origin == "Schweiz" and not in_season:
        return EmissionValues.REGIONAL_NOT_IN_SEASON.value
    elif continent == 'Europe':
        return EmissionValues.EUROPE
    elif transport_type == transport.TransportMode.SHIP.value:
        return EmissionValues.OVERSEAS_SHIP.value
    else:
        return EmissionValues.OVERSEAS_PLANE.value
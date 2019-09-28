
in_season_in_september = [
    #Fruits
    'apple',
    'apricot',
    'pear',
    'blackberry',
    'raspberry',
    'plum',
    #veggies
    'artichoke',
    'aubergine',
    'cauliflower',
    'bean',
    'broccoli',
    'chicory',
    'endive',
    'fennel',
    'cucumber',
    'turnip',
    'carrot',
    'garlic',
    'celery',
    'Lettuce',
    'pumpkin',
    'leek',
    'melon',
    'pepperoni',
    'parsley',
    'radish',
    'beetroot',
    'radish',
    'shallot',
    'chives',
    'spinach',
    'tomato',
    'cabbage',
    'zucchetti',
    'onion',
    'potato'
]


def is_in_season(product: str):
    return product.lower() in [product.lower() for product in in_season_in_september]
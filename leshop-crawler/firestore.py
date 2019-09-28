from google.cloud import firestore


google_cloud_project = 'the-vindicators'
collection_name = 'produce'

db = firestore.Client(google_cloud_project)
collection = db.collection(collection_name)


def save_product(name, **kwargs):
    document_reference = collection.document(name)
    document_reference.set(kwargs)
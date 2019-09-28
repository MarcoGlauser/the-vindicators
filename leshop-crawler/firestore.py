from google.cloud import firestore

collection_name = 'produce'

# Add a new document
google_cloud_project = 'the-vindicators'
db = firestore.Client(google_cloud_project)
collection = db.collection(collection_name)


def save_product(name, **kwargs):
    document_reference = collection.document(name)
    document_reference.set(kwargs)
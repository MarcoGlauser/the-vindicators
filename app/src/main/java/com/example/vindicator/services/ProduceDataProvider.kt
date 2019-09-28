package com.example.vindicator.services

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ProduceDataProvider {

    fun loadProduce(produceName: String): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        val produces = db.collection("produce")
        val ref = produces.document(produceName)

        return ref.get()
    }
}
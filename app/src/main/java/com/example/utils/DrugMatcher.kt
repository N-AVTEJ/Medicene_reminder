package com.example.utils

object DrugMatcher {
    val commonDrugs = listOf(
        "Acetaminophen", "Adderall", "Alprazolam", "Amlodipine", "Amoxicillin",
        "Aspirin", "Atorvastatin", "Azithromycin", "Buprenorphine", "Carvedilol",
        "Cetirizine", "Ciprofloxacin", "Citalopram", "Clopidogrel", "Cyclobenzaprine",
        "Duloxetine", "Escitalopram", "Fluoxetine", "Gabapentin", "Hydrochlorothiazide",
        "Hydrocodone", "Ibuprofen", "Lexapro", "Lisinopril", "Loratadine",
        "Losartan", "Meloxicam", "Metformin", "Metoprolol", "Naproxen",
        "Omeprazole", "Oxycodone", "Pantoprazole", "Prednisone", "Sertraline",
        "Simvastatin", "Tramadol", "Trazodone", "Venlafaxine", "Zolpidem"
    )

    fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length
        var cost = Array(lhsLength + 1) { it }
        var newCost = Array(lhsLength + 1) { 0 }

        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength]
    }

    fun findBestMatch(query: String): String? {
        if (query.isBlank()) return null
        val queryLower = query.lowercase().trim()
        
        // Exact match
        commonDrugs.find { it.lowercase() == queryLower }?.let { return it }
        
        // Substring match
        commonDrugs.find { 
            val itLower = it.lowercase()
            (queryLower.length > 3 && queryLower.contains(itLower)) || (queryLower.length > 3 && itLower.contains(queryLower)) 
        }?.let { return it }

        var bestMatch: String? = null
        var minDistance = Int.MAX_VALUE
        
        for (word in commonDrugs) {
            val distance = levenshtein(queryLower, word.lowercase())
            if (distance < minDistance) {
                minDistance = distance
                bestMatch = word
            }
        }
        
        return if (minDistance <= maxOf(3, queryLower.length / 3)) bestMatch else null
    }
}

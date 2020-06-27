package com.vishi.expensetracker.model

import java.util.*

class ExpenseDetails {

    var id: String = ""
    var amountSpent: Double = 0.0
    var day: Int = 0
    var month: Int = 0
    var year: Int = 0
    var description: String = ""
    var addedBy: String = ""
    var timeAdded: Date = Date()
}
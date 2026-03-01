package com.mediassist.app.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val bloodGroup: String = "",
    val medicalConditions: List<String> = emptyList(),
    val allergies: String = "",
    val preferredHospital: String = "",
    val emergencyNotes: String = ""
) {

    // ✅ Profile completion logic (single source of truth)
    fun isProfileComplete(): Boolean {
        return name.isNotBlank()
                && age > 0
                && gender.isNotBlank()
                && bloodGroup.isNotBlank()
    }
}

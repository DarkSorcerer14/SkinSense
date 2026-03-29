package com.codebros.skinsense.data

/**
 * Provides remedy and information data for detected skin conditions.
 */
object SkinConditionInfo {

    data class ConditionDetails(
        val name: String,
        val description: String,
        val symptoms: List<String>,
        val remedies: List<Remedy>,
        val severity: Severity,
        val doctorAdvice: String
    )

    data class Remedy(
        val name: String,
        val description: String,
        val estimatedCost: String
    )

    enum class Severity(val displayName: String, val color: String) {
        LOW("Low", "#4CAF50"),
        MEDIUM("Medium", "#FF9800"),
        HIGH("High", "#F44336")
    }

    fun getConditionInfo(label: String): ConditionDetails {
        return when (label.lowercase().trim()) {
            "healthy" -> ConditionDetails(
                name = "Healthy Skin",
                description = "Your skin appears to be in good health! No concerning conditions were detected by our analysis.",
                symptoms = listOf(
                    "Even skin tone",
                    "No visible lesions or rashes",
                    "Normal texture"
                ),
                remedies = listOf(
                    Remedy("Moisturizer", "Apply daily moisturizer to maintain skin health", "₹100-200"),
                    Remedy("Sunscreen", "Use SPF 30+ sunscreen daily for protection", "₹150-300"),
                    Remedy("Hydration", "Drink 8-10 glasses of water daily", "Free")
                ),
                severity = Severity.LOW,
                doctorAdvice = "No immediate doctor visit needed. Continue regular skin care routine and protect skin from UV exposure."
            )

            "lupus" -> ConditionDetails(
                name = "Lupus (Suspected)",
                description = "Lupus is an autoimmune disease where the body's immune system attacks its own tissues. A characteristic butterfly-shaped rash may appear across the cheeks and nose.",
                symptoms = listOf(
                    "Butterfly-shaped facial rash",
                    "Skin lesions worsened by sun exposure",
                    "Disk-shaped raised patches",
                    "Fatigue and joint pain",
                    "Photosensitivity"
                ),
                remedies = listOf(
                    Remedy("Sunscreen SPF 50+", "High-protection sunscreen is essential for lupus patients", "₹200-400"),
                    Remedy("Aloe Vera Gel", "Helps soothe irritated skin naturally", "₹80-150"),
                    Remedy("Anti-inflammatory cream", "OTC hydrocortisone cream for mild flares", "₹50-120"),
                    Remedy("Moisturizing lotion", "Fragrance-free moisturizer to keep skin hydrated", "₹100-250")
                ),
                severity = Severity.HIGH,
                doctorAdvice = "⚠️ Please consult a dermatologist or rheumatologist immediately. Lupus requires professional diagnosis and medical treatment. This screening is not a diagnosis."
            )

            "ringworm" -> ConditionDetails(
                name = "Ringworm (Tinea)",
                description = "Ringworm is a common fungal infection that creates ring-shaped rashes on the skin. Despite its name, it is not caused by a worm but by a fungus (dermatophyte).",
                symptoms = listOf(
                    "Ring-shaped, red, scaly patches",
                    "Itching and irritation",
                    "Raised borders with clearing center",
                    "May spread to other body areas",
                    "Possible blistering at edges"
                ),
                remedies = listOf(
                    Remedy("Clotrimazole Cream", "Apply antifungal cream twice daily for 2-4 weeks", "₹50-100"),
                    Remedy("Miconazole Cream", "Alternative antifungal option, apply twice daily", "₹60-120"),
                    Remedy("Terbinafine Cream", "Strong antifungal for resistant cases", "₹80-150"),
                    Remedy("Tea Tree Oil", "Natural antifungal - dilute and apply to affected area", "₹100-200"),
                    Remedy("Antifungal Powder", "Keep affected area dry with medicated powder", "₹40-80")
                ),
                severity = Severity.MEDIUM,
                doctorAdvice = "Ringworm can usually be treated with OTC antifungal creams. If it doesn't improve in 2 weeks, spreads, or appears on the scalp, consult a dermatologist."
            )

            "scalp_infections", "scalp infections" -> ConditionDetails(
                name = "Scalp Infection",
                description = "Scalp infections can be caused by bacteria or fungi and affect the scalp's skin and hair follicles. Common types include folliculitis, seborrheic dermatitis, and tinea capitis.",
                symptoms = listOf(
                    "Itchy, flaky scalp",
                    "Red, inflamed patches on scalp",
                    "Pus-filled bumps or sores",
                    "Hair loss in affected areas",
                    "Crusty or scaly patches",
                    "Tenderness or pain"
                ),
                remedies = listOf(
                    Remedy("Ketoconazole Shampoo", "Medicated antifungal shampoo, use 2-3 times weekly", "₹120-250"),
                    Remedy("Selenium Sulfide Shampoo", "Anti-dandruff shampoo for seborrheic dermatitis", "₹100-200"),
                    Remedy("Tea Tree Oil Shampoo", "Natural antimicrobial shampoo", "₹150-300"),
                    Remedy("Coconut Oil", "Apply warm coconut oil to soothe scalp irritation", "₹50-100"),
                    Remedy("Neem Oil", "Traditional Indian remedy with antimicrobial properties", "₹60-120")
                ),
                severity = Severity.MEDIUM,
                doctorAdvice = "If scalp infection persists for more than 2 weeks, causes significant hair loss, or is accompanied by fever, please visit a dermatologist for proper diagnosis and prescription treatment."
            )

            else -> ConditionDetails(
                name = label.replace("_", " ").replaceFirstChar { it.uppercaseChar() },
                description = "A potential skin condition has been detected. Please consult a healthcare professional for proper diagnosis.",
                symptoms = listOf("Visible skin changes detected"),
                remedies = listOf(
                    Remedy("General Care", "Keep the area clean and moisturized", "₹50-100"),
                    Remedy("Consult Doctor", "Visit a dermatologist for proper diagnosis", "₹200-500")
                ),
                severity = Severity.MEDIUM,
                doctorAdvice = "Please consult a dermatologist for proper diagnosis and treatment recommendations."
            )
        }
    }
}

package com.ascendai.app.data

import com.ascendai.app.model.AscensionGuide
import com.ascendai.app.model.DailyHabit
import com.ascendai.app.model.GuideCategory

object AscensionData {

    val guides = listOf(
        AscensionGuide(
            id = "mewing_orthotropics",
            title = "Mewing & Palatal Posture",
            subtitle = "Reposition maxilla and expand dental arch width",
            category = GuideCategory.JAWLINE,
            readTime = "4 min read",
            difficulty = "Daily Habit",
            impact = "+12 pts Mandible",
            summary = "Upward and forward force on the palatine bone elevates the maxillary complex, sharpening submental skin and defining the jawline.",
            steps = listOf(
                "Say 'SING' and firmly position the posterior third (root) of your tongue on the soft palate.",
                "Create a gentle intra-oral suction hold against the roof of the mouth without teeth clenching.",
                "Maintain continuous nasal breathing 24/7 with lips sealed naturally.",
                "Practice swallow mechanics: Swallow using only the tongue sweep; avoid cheek/lip movement."
            ),
            scientificBasis = "Orthotropic remodeling leverages continuous low-intensity forces against the cranial suture lines, remodeling bone density over 6-18 months."
        ),
        AscensionGuide(
            id = "debloat_protocol",
            title = "Debloating & Sodium Balance",
            subtitle = "Carve cheekbones and flush interstitial water",
            category = GuideCategory.DEBLOAT,
            readTime = "3 min read",
            difficulty = "Strict Protocol",
            impact = "+15 pts Definition",
            summary = "Regulate the aldosterone hormone to purge excess subcutaneous fluid pooling around the buccinator muscles and under-jaw.",
            steps = listOf(
                "Drink 3.5 to 4.0 liters of pure water daily to trigger aldosterone downregulation.",
                "Target 4,500mg Potassium daily using spinach, avocados, potatoes, and potassium citrate.",
                "Eliminate ultra-processed high-sodium foods and artificial MSG flavorings.",
                "Perform cold ice roller glides across cheekbones outward to the preauricular lymph nodes."
            ),
            scientificBasis = "A 4:1 Potassium-to-Sodium intracellular ratio draws water out of the subcutaneous skin layer into muscle cells, uncovering bone definition."
        ),
        AscensionGuide(
            id = "hunter_eyes_periorbital",
            title = "Hunter Eyes & Canthal Tilt",
            subtitle = "Deepen eye socket vector and reduce upper eyelid show",
            category = GuideCategory.EYES,
            readTime = "5 min read",
            difficulty = "Intermediate",
            impact = "+10 pts Eye Area",
            summary = "Maximize compact eye dimensions, positive canthal tilt angle, and eliminate under-eye darkness or hollow shadows.",
            steps = listOf(
                "Sleep exclusively on your back with an ergonomic cervical pillow to eliminate fluid pooling.",
                "Orbicularis oculi training: Isolate and gently lift the lower eyelids for 40 repetitions daily.",
                "Apply chilled Caffeine 5% + EGCG serum every morning directly over orbital margins.",
                "Incorporate micro-dosed Retinol or Bakuchiol around the orbital bone to thicken dermal collagen."
            ),
            scientificBasis = "Infraorbital rim forward projection combined with lower lid tension minimizes scleral show and yields predatory, alert eye geometry."
        ),
        AscensionGuide(
            id = "jawline_masseter_hypertrophy",
            title = "Razor Mandible & Masseter Training",
            subtitle = "Widen jaw angles and sharpen the gonial profile",
            category = GuideCategory.JAWLINE,
            readTime = "4 min read",
            difficulty = "High Intensity",
            impact = "+14 pts Jawline",
            summary = "Hypertrophy of the masseter muscle widens the bigonial diameter to match the bizygomatic cheekbone width for masculine symmetry.",
            steps = listOf(
                "Chew hard mastic or falim gum for 30 minutes every alternate day with even bilateral bite.",
                "Perform chin tucks against wall resistance to strengthen deep neck cervical flexors.",
                "Train neck curls (lying on back, curling head to chest) 3 sets of 20 reps to build neck girth.",
                "Target 10-12% body fat through caloric deficit to eliminate submental neck fat."
            ),
            scientificBasis = "Masseter muscle fibers respond to mechanical loading just like skeletal muscle, developing square lateral angularity at the gonial angle."
        ),
        AscensionGuide(
            id = "glass_skin_collagen",
            title = "Glass Skin & Collagen Matrix",
            subtitle = "Poreless texture, radiant clarity, and even pigmentation",
            category = GuideCategory.SKIN,
            readTime = "5 min read",
            difficulty = "Daily Protocol",
            impact = "+18 pts Skin Quality",
            summary = "Accelerate cellular turnover, stimulate fibroblasts for collagen synthesis, and fortify the lipid moisture barrier.",
            steps = listOf(
                "Morning: Gentle non-stripping cleanser, 10% Vitamin C serum, Ceramide moisturizer, SPF 50+.",
                "Evening: Double cleanse with oil cleanser then gentle foaming wash.",
                "Night treatment: Apply Prescription Tretinoin (0.025% - 0.05%) 3 to 4 nights per week.",
                "Ditch high-glycemic sugar and whey concentrates which spike IGF-1 and sebum production."
            ),
            scientificBasis = "Retinoids bind to RAR nuclear receptors, boosting cellular turnover from 28 days down to 14 days and dramatically boosting pro-collagen I."
        ),
        AscensionGuide(
            id = "hair_facial_harmony",
            title = "Haircut Synergy by Face Shape",
            subtitle = "Optimize facial thirds and accentuate bone structure",
            category = GuideCategory.HAIR,
            readTime = "3 min read",
            difficulty = "Easy Fix",
            impact = "+9 pts Harmony",
            summary = "A tailored hairstyle can instantly elongate a short midface or balance a wide forehead to maximize golden ratio harmony.",
            steps = listOf(
                "Measure facial thirds: Upper (hairline-brow), Middle (brow-nose base), Lower (nose-chin).",
                "For Oval/Round faces: High taper fade with textured volume on top adds vertical structure.",
                "For Oblong/Long faces: Low fade, french crop, or fringe breaks up vertical midface length.",
                "Sharp lineup on temples to direct the viewer's gaze toward cheekbone peaks."
            ),
            scientificBasis = "Visual geometry leverages contrast and vertical-to-horizontal lines to visually align face proportions with the 1.618 golden ratio."
        )
    )

    val defaultHabits = listOf(
        DailyHabit(
            id = "mewing_posture",
            title = "Tongue on Palate (Mewing)",
            description = "Maintain back-third tongue suction against roof of mouth all day",
            category = "Jawline",
            iconName = "face",
            isCompleted = false,
            streakDays = 5
        ),
        DailyHabit(
            id = "water_intake",
            title = "Hydration Flush (3.5L)",
            description = "Drink 3.5L filtered water with electrolytes to prevent facial bloat",
            category = "Debloat",
            iconName = "water",
            isCompleted = false,
            streakDays = 8
        ),
        DailyHabit(
            id = "am_skincare",
            title = "AM Skincare & SPF 50",
            description = "Cleanser, Vitamin C, Light Hydration, Broad Spectrum Sunscreen",
            category = "Skin",
            iconName = "sun",
            isCompleted = false,
            streakDays = 12
        ),
        DailyHabit(
            id = "ice_roll",
            title = "Face Cold Plunge / Ice Roll",
            description = "Drain lymphatic fluid and tighten periorbital tissue upon waking",
            category = "Face",
            iconName = "ice",
            isCompleted = false,
            streakDays = 3
        ),
        DailyHabit(
            id = "neck_posture",
            title = "Chin Tucks & Posture (30 Reps)",
            description = "Reverse forward head posture and elevate the hyoid bone",
            category = "Posture",
            iconName = "fitness",
            isCompleted = false,
            streakDays = 4
        ),
        DailyHabit(
            id = "pm_skincare",
            title = "PM Retinoid & Barrier Cream",
            description = "Apply Tretinoin/Retinol followed by peptide ceramide repair cream",
            category = "Skin",
            iconName = "moon",
            isCompleted = false,
            streakDays = 11
        ),
        DailyHabit(
            id = "back_sleep",
            title = "Sleep on Back (8 Hours)",
            description = "Prevent facial asymmetry and infraorbital wrinkles from pillow friction",
            category = "Eyes",
            iconName = "bed",
            isCompleted = false,
            streakDays = 6
        )
    )
}

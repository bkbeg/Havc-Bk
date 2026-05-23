package com.example.data

enum class DuctType(
    val id: String,
    val displayName: String,
    val description: String,
    val defaultExtra1Label: String? = null,
    val defaultExtra1Value: Double = 0.0,
    val defaultExtra2Label: String? = null,
    val defaultExtra2Value: Double = 0.0,
    val trickTitle: String,
    val trickContent: String
) {
    STRAIGHT(
        id = "straight",
        displayName = "Straight Duct",
        description = "Standard rectangular duct section. Sizing standard is Width × Height with custom joint lengths.",
        trickTitle = "4-Fold Single Sheet Wrap Trick",
        trickContent = "To mark a straight duct on a single sheet of metal in 2 mins: \n\n1. Mark a starting line (FLANGE margin ~ 20mm).\n2. Mark Width, fold, Height, fold, Width, fold, Height.\n3. Add 40mm at the end for PITTSBURGH SEAM overlap.\n4. Score with a screwdriver along marking lines for perfect sharp manual hand-bends using angle steel clamps.",
        defaultExtra1Label = "Seam Allowance (mm)",
        defaultExtra1Value = 50.0
    ),
    ELBOW_90(
        id = "elbow_90",
        displayName = "Elbow 90°",
        description = "Curved transition piece. Perfect flow deflection requires exact radius inner throat and outer heel layouts.",
        defaultExtra1Label = "Throat Radius (mm)",
        defaultExtra1Value = 150.0,
        trickTitle = "Radius Corner Pivot Method",
        trickContent = "Marking the side cheeks easily:\n\n1. Draw a perfect square with length = Inner Radius (R) + Duct Width (W).\n2. Place your trammel/divider at the bottom-left vertex. Swing arc at Radius R for throat line.\n3. Extend trammel to R + W, swing the outer arc for the heel line.\n4. Add 20mm flange allowance at inlet/outlet ends before cutting out. Cheeks are mirrored!",
        defaultExtra2Label = "Seam Depth (mm)",
        defaultExtra2Value = 15.0
    ),
    TAPER(
        id = "taper",
        displayName = "Taper / Reducer",
        description = "Symmetrical reducer or expander duct, centered to adjust air velocity.",
        defaultExtra1Label = "End Width W2 (mm)",
        defaultExtra1Value = 400.0,
        defaultExtra2Label = "End Height H2 (mm)",
        defaultExtra2Value = 500.0,
        trickTitle = "Centerline Crease Alignment",
        trickContent = "Don't mark from the side! \n\n1. Always draw a vertical centerline on your blank sheet first.\n2. Mark W1/2 on both sides of centerline at bottom, and W2/2 at top.\n3. Connect edges to draw symmetrical angled cheeks.\n4. Slant height is the true fabrication length, which is longer than architectural length! Calculated as slant L = √ (L² + ((W1 - W2) / 2)²)."
    ),
    OFFSET(
        id = "offset",
        displayName = "Offset Transition",
        description = "Z-shaped shifting duct piece used to bypass columns, beams or ceiling obstacles.",
        defaultExtra1Label = "Offset Shift S (mm)",
        defaultExtra1Value = 300.0,
        trickTitle = "True Centerline Diagonal Cut",
        trickContent = "Marking offset plates without complex trigonometry:\n\n1. Calculate the true diagonal length: Slanted L = √ (Length² + Shift²).\n2. Cut side panels as simple straight parallelograms of length Slanted L and offset displacement.\n3. Face plates require reverse angle throat cutting so that transition fits flush back to the parallel primary duct."
    ),
    HIGH_SIDE_TAPER(
        id = "high_side_taper",
        displayName = "High-Side Taper",
        description = "Asymmetrical taper with one complete flat side to run flush against walls, columns or concrete ceilings.",
        defaultExtra1Label = "End Width W2 (mm)",
        defaultExtra1Value = 400.0,
        defaultExtra2Label = "End Height H2 (mm)",
        defaultExtra2Value = 500.0,
        trickTitle = "90° Square Flush Side Base",
        trickContent = "Since one side is perfectly straight (aligned flush with wall):\n\n1. Establish one edge of blank sheet as the square base - do NOT cut this side!\n2. Mark all width taper reductions (W1 - W2) entirely on the opposite edge.\n3. Side plates will have asymmetric angles: Left plate is square, Right plate carries 100% of the slant slope."
    ),
    PLENUM(
        id = "plenum",
        displayName = "Plenum Box",
        description = "A terminal pressure-equalizing air box with intake collars and grille mounts.",
        defaultExtra1Label = "End Cap Depth (mm)",
        defaultExtra1Value = 20.0,
        trickTitle = "The 5-Sided Box Unfolded Layout",
        trickContent = "Standard plenums are five-sided bins:\n\n1. Cut a complete flat bottom & top plus left and right sides.\n2. Leave the front open for grilles, and secure a complete single-square End Cap with a hand-formed riveted flange.\n3. Subtract joint hole areas from the final sheet square meter count."
    ),
    DUMMY_SPACER(
        id = "dummy",
        displayName = "Dummy Spacer",
        description = "Flexible segment of straight ducting used for fine-tuning lengths or coupling sections in duct corridors.",
        defaultExtra1Label = "Flange Grip (mm)",
        defaultExtra1Value = 10.0,
        trickTitle = "Leftover Strip Recycling Trick",
        trickContent = "Never waste fresh sheet metal for dummy spacers!\n\n1. Spacers can be made by joining 2 L-shaped scrap strips.\n2. Add custom collar connectors (slip joints / drive slips) to bypass precision cutting.\n3. Keep on-site cutting allowance of +10mm to shave with snips during direct installation."
    ),
    SHOE_COLLAR(
        id = "shoe_collar",
        displayName = "Shoe Collar Take-Off",
        description = "Angled tap or branch adapter that scoops airflow into a secondary branch without excessive pressure loss.",
        defaultExtra1Label = "Shoe Projection (mm)",
        defaultExtra1Value = 100.0,
        trickTitle = "45° Air-Scoop Projection Cut",
        trickContent = "Marking a shoe collar scoop branch:\n\n1. Draw the cylindrical wrap template with height H.\n2. Cut a skewed curve trailing 45 degrees towards upstream airflow.\n3. Snip 15mm deep locking tabs ('dovetails') along insertion hole perimeter, then flare alternate tabs outwards & bend the rest inwards inside main duct to lock tightly."
    ),
    TEE_PIECE(
        id = "tee_piece",
        displayName = "Tee Piece (3-Way)",
        description = "Splits main airflow into two opposite directions at a balanced 90° T-junction.",
        defaultExtra1Label = "Branch Width W2 (mm)",
        defaultExtra1Value = 400.0,
        defaultExtra2Label = "Branch Length L2 (mm)",
        defaultExtra2Value = 300.0,
        trickTitle = "Collar Flange Overlap Trick",
        trickContent = "The easiest Tee junction is formed by mounting an independent branch onto a straight main run:\n\n1. Cut main duct opening slightly smaller than Branch W2.\n2. Leave 15 to 20mm extra layout flange on the branch piece's mating surface.\n3. Cut branch corners ('notching'), fold outward, apply mastic sealant, and rivet directly onto outer face surrounding main duct hole."
    ),
    YEE_PIECE(
        id = "yee_piece",
        displayName = "Yee Piece (Pant Wye)",
        description = "Splits airflow at gentle balanced angles (e.g. 30° / 45°), reducing friction loss in high pressure lines.",
        defaultExtra1Label = "Split Width W2 (mm)",
        defaultExtra1Value = 400.0,
        defaultExtra2Label = "Split Angle (deg)",
        defaultExtra2Value = 45.0,
        trickTitle = "Crotch Alignment & V-Plate Trick",
        trickContent = "The crotch split of a Pant-Wye is the most vulnerable seam:\n\n1. Cut the inner 'V' plate of pants first.\n2. Mark the throat curve on top/bottom cheek triangles.\n3. Seam the split using a hand-riveted wrapper strip with heavy silicone backing to stop bypass draft leaks.\n4. Keep angle bends symmetric to preserve streamlined airflow."
    );

    companion object {
        fun fromId(id: String): DuctType {
            return values().firstOrNull { it.id == id } ?: STRAIGHT
        }
    }
}

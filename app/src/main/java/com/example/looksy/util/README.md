# OutfitCompatibilityCalculator Algorithm

## Overview

`OutfitCompatibilityCalculator` is an algorithm that evaluates the compatibility of clothing combinations. It calculates a compatibility score between 0-100 for a given clothing combination (`OutfitResult`).

## Key Features

### 1. Compatibility Score Calculation
- Input: `OutfitResult` object (top, pants, skirt, jacket, dress)
- Output: Integer score between 0-100
- Higher scores indicate better matching combinations

### 2. Essential Rule Validation
The following rules result in an immediate 0 score if violated:
- **Dress and pants/skirt cannot be worn together**: When wearing a dress, pants or skirt cannot be worn simultaneously
- **Top or dress required**: At least one of top or dress must be present

## Score Calculation Method

The final score is calculated as a weighted average of the following 5 factors:

| Factor | Weight | Description |
|--------|--------|-------------|
| Season Compatibility | 30% | Evaluates how well all clothing items' seasons match |
| Material Compatibility | 25% | Evaluates how well clothing material combinations match |
| Type Combination | 20% | Evaluates whether clothing type combinations are logical |
| Size Consistency | 15% | Evaluates how consistent all clothing sizes are |
| Cleanliness | 10% | Evaluates whether all clothing items are clean |

### 1. Season Compatibility (30%)

Compares seasonal usage information of clothing items to calculate the score:

- **100 points**: All clothing items have the same season
- **70 points**: Two seasons present, one of which is `inBetween` (transitional season)
- **40 points**: Two different seasons
- **20 points**: Three or more different seasons

### 2. Material Compatibility (25%)

Evaluates clothing material combinations. Calculates combination scores for all clothing pairs and takes the average:

#### Perfect Combinations (100 points)
- Same material
- Well-matched combinations:
  - Cotton + Jeans
  - Cotton + Linen
  - Wool + Cashmere
  - Silk + Cashmere
  - Polyester + Cotton
  - Linen + Cotton
  - Wool + Polyester

#### Neutral Combinations (60 points)
- Combinations including Cotton or Polyester (excluding perfect combinations above)

#### Incompatible Combinations (30 points)
- Fur + Linen
- Jeans + Silk

#### Default Score (60 points)
- Other combinations not listed above

### 3. Type Combination (20%)

Evaluates whether clothing type combinations are logical:

- **100 points**: Dress + Jacket
- **90 points**: Dress only (no jacket)
- **100 points**: Top + Bottom (pants/skirt) + Jacket
- **100 points**: Top + Bottom (pants/skirt)
- **50 points**: Top only or Bottom only
- **60 points**: Other combinations

### 4. Size Consistency (15%)

Evaluates how consistent all clothing sizes are. Converts sizes to numbers and calculates differences:

- **100 points**: All clothing items have the same size
- **80 points**: Size difference of 1 step or less
- **60 points**: Size difference of 2 steps or less
- **40 points**: Size difference of 3 steps or less
- **20 points**: Size difference of 4 steps or more

#### Size Conversion Standards
- Standard sizes: XS(1), S(2), M(3), L(4), XL(5)
- European sizes: 34-36(1), 38(2), 40(3), 42(4), 44(5), 46(6), 48(7), 50(8), 52(9), 54(10), 56(11), 58(12), 60(13)

### 5. Cleanliness (10%)

Evaluates whether all clothing items are clean:

- **100 points**: All clothing items are clean (`clean = true`)
- **0 points**: Any dirty clothing item present (`clean = false`)

## Usage Example

```kotlin
import com.example.looksy.util.OutfitCompatibilityCalculator
import com.example.looksy.util.OutfitResult

// Create clothing combination
val outfit = OutfitResult(
    top = someTopClothes,
    pants = somePantsClothes,
    skirt = null,
    jacket = someJacketClothes,
    dress = null
)

// Calculate compatibility score
val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)
println("Compatibility score: $score/100")
```

## Testing

Comprehensive test cases are included to verify the algorithm's accuracy:

- Empty wardrobe combination tests
- Rule violation tests (dress + pants, dress + skirt, no top/dress)
- Perfect combination tests
- Season mismatch tests
- Material combination tests
- Size consistency tests
- Cleanliness tests

Test file: `OutfitCompatibilityCalculatorTest.kt`

## Algorithm Characteristics

1. **Rule-based validation**: Logically impossible combinations are immediately scored 0
2. **Weighted average**: Different weights applied based on each factor's importance
3. **Flexible evaluation**: Considers flexible elements such as `inBetween` seasons
4. **Practical criteria**: Material and type combination evaluation reflecting real fashion combination principles

## Areas for Improvement

- Seasonal weight adjustment (e.g., increase material compatibility weight in winter)
- Add color combination evaluation
- Add style consistency evaluation
- User preference learning functionality

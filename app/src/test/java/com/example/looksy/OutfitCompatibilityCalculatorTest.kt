package com.example.looksy

import com.example.looksy.data.model.Clothes
import com.example.looksy.data.model.Material
import com.example.looksy.data.model.Season
import com.example.looksy.data.model.Size
import com.example.looksy.data.model.Type
import com.example.looksy.data.model.WashingNotes
import com.example.looksy.util.OutfitCompatibilityCalculator
import com.example.looksy.util.OutfitResult
import org.junit.Assert.*
import org.junit.Test

class OutfitCompatibilityCalculatorTest {

    // Helper function to create test clothes
    private fun createClothes(
        id: Int = 0,
        size: Size = Size._M,
        season: Season = Season.Summer,
        type: Type = Type.Tops,
        material: Material = Material.Cotton,
        clean: Boolean = true
    ): Clothes {
        return Clothes(
            id = id,
            size = size,
            seasonUsage = season,
            type = type,
            material = material,
            clean = clean,
            washingNotes = WashingNotes.Temperature30,
            imagePath = "",
            isSynced = false
        )
    }

    @Test
    fun `calculateCompatibilityScore should return 0 for empty outfit`() {
        // Given
        val outfit = OutfitResult(null, null, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then
        assertEquals(0, score)
    }

    @Test
    fun `calculateCompatibilityScore should return 0 when dress is combined with pants`() {
        // Given - 규칙 위반: 드레스와 바지를 함께 입음
        val dress = createClothes(id = 1, type = Type.Dress)
        val pants = createClothes(id = 2, type = Type.Pants)
        val outfit = OutfitResult(null, pants, null, null, dress)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then
        assertEquals(0, score)
    }

    @Test
    fun `calculateCompatibilityScore should return 0 when dress is combined with skirt`() {
        // Given - 규칙 위반: 드레스와 스커트를 함께 입음
        val dress = createClothes(id = 1, type = Type.Dress)
        val skirt = createClothes(id = 2, type = Type.Skirt)
        val outfit = OutfitResult(null, null, skirt, null, dress)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then
        assertEquals(0, score)
    }

    @Test
    fun `calculateCompatibilityScore should return 0 when no top or dress`() {
        // Given - 규칙 위반: 상의나 드레스가 없음
        val pants = createClothes(id = 1, type = Type.Pants)
        val outfit = OutfitResult(null, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then
        assertEquals(0, score)
    }

    @Test
    fun `calculateCompatibilityScore should return high score for perfect outfit with same season`() {
        // Given - 완벽한 조합: 같은 계절, 잘 어울리는 소재, 논리적 조합
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            season = Season.Summer,
            material = Material.Cotton,
            size = Size._M
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            season = Season.Summer,
            material = Material.jeans,
            size = Size._M
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - 높은 점수 예상 (80점 이상)
        assertTrue("Score should be high for perfect outfit", score >= 80)
    }

    @Test
    fun `calculateCompatibilityScore should return high score for dress with jacket`() {
        // Given - 드레스 + 재킷 조합
        val dress = createClothes(
            id = 1,
            type = Type.Dress,
            season = Season.Summer,
            material = Material.Cotton,
            size = Size._M
        )
        val jacket = createClothes(
            id = 2,
            type = Type.Jacket,
            season = Season.Summer,
            material = Material.Cotton,
            size = Size._M
        )
        val outfit = OutfitResult(null, null, null, jacket, dress)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - 높은 점수 예상
        assertTrue("Score should be high for dress with jacket", score >= 80)
    }

    @Test
    fun `calculateCompatibilityScore should return lower score for different seasons`() {
        // Given - 다른 계절 조합
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            season = Season.Summer,
            material = Material.Cotton
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            season = Season.Winter,
            material = Material.Cotton
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - 계절이 다르므로 점수가 낮아야 함
        assertTrue("Score should be lower for different seasons", score < 70)
    }

    @Test
    fun `calculateCompatibilityScore should return 0 when clothes are not clean`() {
        // Given - 더러운 옷
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            season = Season.Summer,
            material = Material.Cotton,
            clean = false
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            season = Season.Summer,
            material = Material.Cotton
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - 청결도 점수가 0이므로 전체 점수가 낮아야 함
        assertTrue("Score should be lower when clothes are not clean", score < 50)
    }

    @Test
    fun `calculateCompatibilityScore should return high score for good material combinations`() {
        // Given - 잘 어울리는 소재 조합 (Cotton + Jeans)
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            material = Material.Cotton,
            season = Season.Summer
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            material = Material.jeans,
            season = Season.Summer
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - 좋은 소재 조합이므로 높은 점수
        assertTrue("Score should be high for good material combinations", score >= 70)
    }

    @Test
    fun `calculateCompatibilityScore should return lower score for incompatible materials`() {
        // Given - 어울리지 않는 소재 조합 (Jeans + Silk)
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            material = Material.jeans,
            season = Season.Summer
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            material = Material.silk,
            season = Season.Summer
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - 어울리지 않는 소재이므로 점수가 낮아야 함
        assertTrue("Score should be lower for incompatible materials", score < 70)
    }

    @Test
    fun `calculateCompatibilityScore should return high score for similar sizes`() {
        // Given - 비슷한 사이즈
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            size = Size._M,
            season = Season.Summer
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            size = Size._M,
            season = Season.Summer
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - 같은 사이즈이므로 높은 점수
        assertTrue("Score should be high for similar sizes", score >= 70)
    }

    @Test
    fun `calculateCompatibilityScore should return lower score for different sizes`() {
        // Given - 매우 다른 사이즈
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            size = Size._XS,
            season = Season.Summer
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            size = Size._XL,
            season = Season.Summer
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - 사이즈 차이가 크므로 점수가 낮아야 함
        assertTrue("Score should be lower for different sizes", score < 80)
    }

    @Test
    fun `calculateCompatibilityScore should handle inBetween season correctly`() {
        // Given - inBetween 계절 포함
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            season = Season.Summer,
            material = Material.Cotton
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            season = Season.inBetween,
            material = Material.Cotton
        )
        val outfit = OutfitResult(top, pants, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - inBetween은 유연하게 처리되어야 함
        assertTrue("Score should be reasonable for inBetween season", score >= 50)
    }

    @Test
    fun `calculateCompatibilityScore should return high score for complete outfit with jacket`() {
        // Given - 완전한 코디: 상의 + 하의 + 재킷
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            season = Season.Summer,
            material = Material.Cotton,
            size = Size._M
        )
        val pants = createClothes(
            id = 2,
            type = Type.Pants,
            season = Season.Summer,
            material = Material.jeans,
            size = Size._M
        )
        val jacket = createClothes(
            id = 3,
            type = Type.Jacket,
            season = Season.Summer,
            material = Material.Cotton,
            size = Size._M
        )
        val outfit = OutfitResult(top, pants, null, jacket, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - 완전한 코디이므로 매우 높은 점수
        assertTrue("Score should be very high for complete outfit", score >= 85)
    }

    @Test
    fun `calculateCompatibilityScore should return score for top only outfit`() {
        // Given - 상의만 있는 경우
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            season = Season.Summer,
            material = Material.Cotton
        )
        val outfit = OutfitResult(top, null, null, null, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - 불완전한 코디이므로 점수가 낮아야 함
        assertTrue("Score should be lower for incomplete outfit", score < 70)
    }

    @Test
    fun `calculateCompatibilityScore should return high score for wool and cashmere combination`() {
        // Given - 잘 어울리는 소재 조합 (Wool + Cashmere)
        val top = createClothes(
            id = 1,
            type = Type.Tops,
            material = Material.Wool,
            season = Season.Winter
        )
        val jacket = createClothes(
            id = 2,
            type = Type.Jacket,
            material = Material.cashmere,
            season = Season.Winter
        )
        val outfit = OutfitResult(top, null, null, jacket, null)

        // When
        val score = OutfitCompatibilityCalculator.calculateCompatibilityScore(outfit)

        // Then - 좋은 소재 조합이므로 높은 점수
        assertTrue("Score should be high for wool and cashmere", score >= 70)
    }
}

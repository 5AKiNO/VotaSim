package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCandidatePhotoPartyFolderName() {
    val candACR = CandidateModel(
      id = "c1",
      name = "Candidato ACR",
      party = "Agrupación Cívica Roja",
      list = "1",
      initials = "ACR"
    )
    assertEquals("acr", CandidatePhotoManager.getPartyFolderName(candACR))

    val candMCA = CandidateModel(
      id = "c2",
      name = "Candidato MCA",
      party = "Movimiento Ciudadano Azul",
      list = "2",
      initials = "MCA"
    )
    assertEquals("mca", CandidatePhotoManager.getPartyFolderName(candMCA))

    assertTrue(CandidatePhotoManager.matchesParty(candACR, "acr"))
    assertTrue(CandidatePhotoManager.matchesParty(candACR, "lista_1"))
    assertTrue(CandidatePhotoManager.matchesParty(candMCA, "mca"))
    assertTrue(CandidatePhotoManager.matchesParty(candMCA, "lista_2"))
  }
}

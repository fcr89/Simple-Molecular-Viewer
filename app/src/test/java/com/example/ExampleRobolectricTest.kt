package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PresetProteins
import com.example.model.SecondaryStructure
import com.example.parser.PdbParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Fold3D", appName)
  }

  @Test
  fun `pdb parser parses crambin preset correctly`() {
    val structure = PdbParser.parse(PresetProteins.CRAMBIN_PDB, "1CRN")
    assertNotNull(structure)
    assertEquals("1CRN", structure.id)
    assertTrue("Atoms should not be empty", structure.allAtoms.isNotEmpty())
    assertTrue("Total residues should be > 0", structure.totalResidues > 0)
    assertTrue("Helix count should be > 0", structure.helixCount > 0)
  }
}

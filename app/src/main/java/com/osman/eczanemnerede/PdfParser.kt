package com.osman.eczanemnerede
import android.icu.util.Calendar
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.ByteArrayInputStream
import java.io.IOException
data class WeekData(
    val weekNumber: Int, // Add fields as needed
    val districts: List<DistrictData>
)

data class DistrictData(
    val districtName: String,
    val pharmacyDataList: List<PharmacyData>
)

data class PharmacyData(
    val date: String,
    val pharmacyName: String,
    val address: String
)
class PdfParser {
    companion object {
        fun parsePdf(pdfContent: String): List<WeekData> {
            val weekDataList = mutableListOf<WeekData>()

            // Iterate over pages
            // ...

            for (pageText in pages) {
                val weekData = parsePage(pageText)
                weekDataList.add(weekData)
            }

            return weekDataList
        }

        private fun parsePage(pageText: String): WeekData {
            val weekData = WeekData(weekNumber = 1, districts = mutableListOf())

            // Split pageText into rows and columns
            val rows = pageText.split("\n")
            // ...

            // Iterate over rows and columns to extract data
            for (row in rows) {
                val columns = row.split("\t") // Adjust separator based on your PDF structure
                // ...

                // Extract data and populate DistrictData and PharmacyData objects
                val districtName = columns[0]
                val date = columns[1]
                val pharmacyName = columns[2]
                val address = columns[3]

                val pharmacyData = PharmacyData(date, pharmacyName, address)

                // Find or create the corresponding DistrictData object
                val existingDistrict = weekData.districts.find { it.districtName == districtName }
                if (existingDistrict != null) {
                    existingDistrict.pharmacyDataList.add(pharmacyData)
                } else {
                    val newDistrict = DistrictData(districtName, mutableListOf(pharmacyData))
                    weekData.districts.add(newDistrict)
                }
            }

            return weekData
        }
    }
}
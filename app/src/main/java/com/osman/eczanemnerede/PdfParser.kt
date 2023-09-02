package com.osman.eczanemnerede
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.ByteArrayInputStream
import java.io.IOException

data class WeekData(
    val weekNumber: Int,
    val districts: MutableList<DistrictData> // Change List to MutableList
)


data class DistrictData(
    val districtName: String,
    val pharmacyDataList: MutableList<PharmacyData> // Use MutableList instead of List
)

data class PharmacyData(
    val date: String,
    val pharmacyName: String,
    val address: String
)

class PdfParser {
    companion object {
        fun getPharmacyNames(pdfContent: String): List<String> {
            val weekDataList = parsePdf(pdfContent)
            val pharmacyNames = mutableListOf<String>()

            // Iterate through the parsed data to extract pharmacy names
            for (weekData in weekDataList) {
                for (districtData in weekData.districts) {
                    for (pharmacyData in districtData.pharmacyDataList) {
                        val pharmacyName = pharmacyData.pharmacyName
                        pharmacyNames.add(pharmacyName)
                    }
                }
            }

            return pharmacyNames
        }
        fun parsePdf(pdfContent: String): List<WeekData> {
            val weekDataList = mutableListOf<WeekData>()

            try {
                val document = PDDocument.load(ByteArrayInputStream(pdfContent.toByteArray()))
                val stripper = PDFTextStripper()

                for (pageNum in 0 until document.numberOfPages) {
                    stripper.startPage = pageNum + 1
                    stripper.endPage = pageNum + 1

                    val pageText = stripper.getText(document)

                    val weekData = parsePage(pageText)
                    weekDataList.add(weekData)
                }

                document.close()
            } catch (e: IOException) {
                e.printStackTrace()
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

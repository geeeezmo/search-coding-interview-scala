package albums.challenge

import albums.challenge.models._
import org.springframework.stereotype.Service

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

@Service
class SearchService {
  private val PriceRangeRegex = """^(\d+)\s*-\s*(\d+)$""".r

  def search(
      entries: List[Entry],
      query: String,
      year: List[String] = List.empty,
      price: List[String] = List.empty,
  ): Results = {
    val (yearParseErrors, years) = year.partitionMap(parseYear)

    val (priceRangeParseErrors, priceRanges) = price.partitionMap(parsePriceRange)

    val allErrors = yearParseErrors ++ priceRangeParseErrors
    if (allErrors.nonEmpty) {
      throw new IllegalArgumentException(s"errors: ${allErrors.mkString("[", ", ", "]")}")
    }

    val normalizedQuery = query.trim
    val queryFilteredEntries = if (normalizedQuery.isEmpty) {
      entries
    } else {
      val queryTokens = tokenizeString(normalizedQuery)
      entries.filter(entry => tokenizeString(entry.title).exists(queryTokens.contains))
    }

    val (filteredEntries, priceFacetData, yearFacetData) =
      queryFilteredEntries
        // build three collections in one pass, avoiding re-filtering:
        // 1. entries filtered by both years and price ranges
        // 2. price ranges from filtered entries
        // 3. years from filtered entries
        .foldLeft(
          (
            List.empty[Entry],
            List.empty[PriceRange],
            List.empty[Int],
          ),
        ) { case ((filteredAll, priceList, yearList), entry) =>
          val releaseYear = Try(
            LocalDateTime.parse(entry.releaseDate, DateTimeFormatter.ISO_DATE_TIME),
          ).toOption.map(_.getYear)
          val priceRange = PriceRange.fromPrice(entry.price)

          val hasYear = years.isEmpty || years.exists(releaseYear.contains)
          val hasPriceRange = priceRanges.isEmpty || priceRanges.contains(priceRange)

          (
            if (hasYear && hasPriceRange) filteredAll :+ entry else filteredAll,
            if (hasYear) priceList :+ priceRange else priceList,
            if (hasPriceRange) yearList ++ releaseYear else yearList,
          )
        }

    // build facets
    val yearFacet = yearFacetData
      .groupBy(year => year)
      .toList
      .sortBy { case (year, _) => year }
      .reverse
      .map { case (year, bucket) =>
        Facet(year.toString, bucket.size)
      }

    val priceRangeFacet = priceFacetData
      .groupBy(range => range)
      .toList
      .sortBy { case (range, _) => range.start }
      .map { case (range, bucket) =>
        Facet(range.toString, bucket.size)
      }

    Results(
      items = filteredEntries,
      facets = Map("price" -> priceRangeFacet, "year" -> yearFacet),
      query = query,
    )
  }

  private def tokenizeString(str: String): List[String] =
    str.toLowerCase.split(" ").map(_.trim).toList.filterNot(_.isEmpty)

  private def parseYear(yearStr: String): Either[String, Int] =
    yearStr.trim.toIntOption.map(Right(_)).getOrElse(Left(s"invalid year: $yearStr"))

  private def parsePriceRange(rangeStr: String): Either[String, PriceRange] = {
    val rangeOpt = for {
      rMatch <- PriceRangeRegex.findFirstMatchIn(rangeStr)
      start <- rMatch.group(1).toFloatOption
      end <- rMatch.group(2).toFloatOption
      range <- if (start >= 0 && end > 0 && end > start) Some(PriceRange(start, end)) else None
    } yield Right(range)

    rangeOpt.getOrElse(Left(s"invalid price range: $rangeStr"))
  }
}

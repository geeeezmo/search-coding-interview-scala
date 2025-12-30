package albums.challenge.models

case class PriceRange(start: Float, end: Float) {
  override def toString: String = s"${start.toInt} - ${end.toInt}"
}

object PriceRange {
  def fromPrice(price: Float, width: Int = 5): PriceRange =
    PriceRange(
      start = price - (price % width),
      end = price - (price % width) + width,
    )
}

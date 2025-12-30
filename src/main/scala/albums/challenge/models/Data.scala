package albums.challenge.models

import com.fasterxml.jackson.annotation.JsonProperty

case class Data(feed: Data.Feed = Data.Feed()) {
  def convert(): List[Entry] = feed.entry
    .map { entry =>
      Entry(
        entry.title.label,
        entry.price.attributes.amount.toFloat,
        entry.releaseDate.label,
        entry.link.attributes.href,
        entry.images.head.label,
      )
    }
}

object Data {
  case class Feed(entry: List[Feed.Entry] = List.empty) {
    def this() = this(List())
  }

  object Feed {
    case class Entry(
        title: Label,
        link: Link,
        @JsonProperty("im:image") images: List[Label],
        @JsonProperty("im:price") price: Price,
        @JsonProperty("im:releaseDate") releaseDate: Label,
    )

    case class Label(label: String)

    case class Price(label: String, attributes: PriceAttributes)

    case class PriceAttributes(amount: String)

    case class Link(attributes: Attributes)

    case class Attributes(href: String)
  }
}

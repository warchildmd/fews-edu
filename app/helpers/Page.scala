package helpers

case class Page[A](data: Seq[A], current_page: Int, from: Int, to: Int, total: Int) {
  lazy val prev_page = Option(current_page - 1).filter(_ >= 0)
  lazy val next_page = Option(current_page + 1).filter(_ => to < total)
}

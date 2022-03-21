package edu.duke.compsci516.models.entity

case class Crew(
    id: Int,
    birthday: Option[java.sql.Date] = None,
    knowForDepartment: Option[String] = None,
    name: String,
    gender: Int,
    biography: Option[String] = None,
    placeOfBirth: Option[String] = None,
    profilePath: Option[String] = None
)

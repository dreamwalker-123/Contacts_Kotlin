package contacts

import java.text.SimpleDateFormat
import java.util.*
import java.io.File
import java.time.Clock

private val contacts = mutableListOf<Base>()
const val fileName = "myFile.txt"
val myFile = File(fileName)

fun main() = userInput()

fun userInput() {
    println("open phonebook.db\n")
    while (true) {
        when (val i = input("[menu] Enter action (add, list, search, count, exit):")) {
            "exit" -> break
            "list", "search", "count" -> if (contacts.isEmpty()) println("0 records") else controller(i)
            else -> controller(i)
        }
    }
    myFile.delete()
}

fun controller(action: String) {
    while (true) {
        when(action) {
            "add" -> { addType(); break}
            "list" -> {
                listContacts()
                val command = input("[list] Enter action ([number], back):")
                if (command == "back") break
                val el = contacts[command.toInt() - 1]
                el.fullInfo()
                while (true) {
                    when(input("[record] Enter action (edit, delete, menu):")) {
                        "menu" -> break
                        "edit" -> {
                            if (el is Person) {
                                val field = input("Select a field (name, surname, birth, gender, number):")
                                val newValue = input("Enter $field")
                                el.setProperty(el, field, newValue)
                            } else if (el is Organization) {
                                val field = input("Select a field (name, address, number):")
                                val newValue = input("Enter $field")
                                el.setProperty(el, field, newValue)
                            }
                            println("Saved")
                            el.fullInfo()
                            el.timeLastEdit = Clock.systemDefaultZone().toString()
                        }
                        "delete" -> {
                            if (el is Person) {
                                val field = input("Select a field (name, surname, birth, gender, number):")
                                val newValue = "[no data]"
                                el.setProperty(el, field, newValue)
                            } else if (el is Organization) {
                                val field = input("Select a field (name, address, number):")
                                val newValue = "[no data]"
                                el.setProperty(el, field, newValue)
                            }
                            el.timeLastEdit = Clock.systemDefaultZone().toString()
                        }
                    }
                    writeInFile()
                }
                break
            }
            "search" -> {
                while (true) {
                    val query = input("Enter search query:")
                    if (query == "exit") break
                    val regex = """.*($query).*""".toRegex()
                    var numberOfCoincidences = 0
                    val list = mutableListOf<Int>()
                    contacts.forEachIndexed { i, it ->
                        if (it.name.lowercase().matches(regex)) {
                            numberOfCoincidences++
                            list.add(i)
                        }
                        if (it is Person && it.surname.lowercase().matches(regex)) {
                            numberOfCoincidences++
                            list.add(i)
                        }
                        if (it.number.lowercase().matches(regex)) {
                            numberOfCoincidences++
                            list.add(i)
                        }
                    }
                    println("Found $numberOfCoincidences results:")
                    list.forEachIndexed { i, index -> println("${i + 1}. ${contacts[index]}") }

                    when (val action1 = input("\n[search] Enter action ([number], back, again):")) {
                        "back" -> break
                        "again" -> continue
                        else -> {
                            contacts[action1.toInt() - 1].fullInfo()
                            edit(contacts[action1.toInt()])
                            break
                        }
                    }
                }
                break
            }
            "count" -> { countElements(); break}
            "exit" -> break
        }
    }
    return
}
fun edit(el: Base) {
    while (true) {
        when(input("[record] Enter action (edit, delete, menu):")) {
            "menu" -> break
            "edit" -> {
                if (el is Person) {
                    val field = input("Select a field (name, surname, birth, gender, number):")
                    val newValue = input("Enter $field")
                    el.setProperty(el, field, newValue)
                } else if (el is Organization) {
                    val field = input("Select a field (name, address, number):")
                    val newValue = input("Enter $field")
                    el.setProperty(el, field, newValue)
                }
                println("Saved")
                el.fullInfo()
                el.timeLastEdit = Clock.systemDefaultZone().toString()
            }
            "delete" -> {
                if (el is Person) {
                    val field = input("Select a field (name, surname, birth, gender, number):")
                    val newValue = "[no data]"
                    el.setProperty(el, field, newValue)
                } else if (el is Organization) {
                    val field = input("Select a field (name, address, number):")
                    val newValue = "[no data]"
                    el.setProperty(el, field, newValue)
                }
                el.timeLastEdit = Clock.systemDefaultZone().toString()
            }
        }
        writeInFile()
    }
    return
}
fun writeInFile() {
    contacts.forEach { myFile.writeText(it.toString()) }
}
fun input(prompt: String) = println(prompt).run { readln() }
fun checkNumber(number: String): String =
    if (number.matches(Regex("""\+?[\s-]?((\(\w+\)[\s-]\w{2,})|(\w+[\s-]\(\w{2,}\))|(\(?\w+\)?))([\s-]\w{2,})*"""))) {
        number
    } else "[no number]".also { println("Wrong number format!") }
fun checkDate(date: String) = date.ifBlank { "[no data]".also { println("Bad birth date!") } }
fun checkGender(gender: String) = if (gender in setOf("M", "F")) gender else "[no data]".also { println("Bad gender!") }

fun addElement(type: Int) {
    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(Date())
    if (type == 1) {
        val (name, surname) = listOf(input("Enter the name:"), input("Enter the surname:"))
        val birthDate = checkDate(input("Enter the birth date:"))
        val gender = checkGender(input("Enter the gender (M, F):"))
        val number = checkNumber(input("Enter the number:"))
        contacts.add(Person(name, surname, birthDate, gender, number, date, date))
    } else {
        val (name, address) = listOf(input("Enter the organization name:"), input("Enter the address:"))
        val number = checkNumber(input("Enter the number:"))
        contacts.add(Organization(name, address, number, date, date))
    }
    println("The record added.\n")
}

fun addType() = if (input("Enter the type (person, organization):") == "person") addElement(1) else addElement(2)
fun countElements() = println("The Phone Book has ${contacts.size} records.")
fun listContacts() = contacts.forEachIndexed { i, p -> println("${i + 1}. $p") }

fun Base.setProperty(element: Any, fieldElement: String, value: String) {
    val propertySet = when (fieldElement) {
        "number" -> Base::class.java.getDeclaredField(fieldElement)
        else -> element::class.java.getDeclaredField(fieldElement)
    }
    propertySet.isAccessible = true
    propertySet.set(this, if (fieldElement == "number") checkNumber(value) else value)
}

open class Base(var name: String, var number: String, val timeCreated: String, var timeLastEdit: String) {
    open fun fullInfo() {}
}

class Person(name: String, var surname: String, private var birthDate: String,
             private var gender: String, number: String, timeCreated: String, timeLastEdit: String
) : Base(name, number, timeCreated, timeLastEdit) {
    override fun toString(): String = "$name $surname"
    override fun fullInfo() = println( "Name: $name\nSurname: $surname\nBirth date: $birthDate\nGender: $gender\n" +
            "Number: $number\nTime created: $timeCreated\nTime last edit: $timeLastEdit\n" )
}

class Organization(name: String, private var address: String, number: String, timeCreated: String,
                   timeLastEdit: String ) : Base(name, number, timeCreated, timeLastEdit) {
    override fun toString(): String = name
    override fun fullInfo() = println( "Organization name: $name\nAddress: $address\nNumber: $number\n" +
            "Time created: $timeCreated\nTime last edit: $timeLastEdit\n" )
}
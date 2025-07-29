package ecommerce.study

import jakarta.persistence.*

/* JPA = ORM (database mapping)

since we added `plugin.jpa`(made by java)
    - need no arg constructore

but in kotlin
    - common to have primary constructor
    -> burden / boiler plate

so kotlin provide this `plugin`
    - it des it in compile time

+advance
    - add api structure
    - proxy class can be also added
this plugin automatically: open class, and so on


 */

/*we can make ver1: but boilerplate, not necessary when have `plugin.jpa`
@Entity // (1)
@Table(name = "station") // (2)name can be changed
open class Station(
    @Column(name = "name", nullable = false) // (3)db col
    var name: String,

    @Id // (4) primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // (5) idle strategy
    val id: Long? = null,
    ){
    constructor()
} // (6)
*/

//ver2
@Entity // (1)
@Table(name = "station info") // (2)name can be changed
class Station(
    @Column(name = "station_name", nullable = false) // (3)db col
    var name: String,

    @Column(nullable = false) // (3)db col
    var price: Int,

    @Id // (4) primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // (5) idle strategy
    val id: Long? = null,

) {
    fun changeName(name: String) {
        this.name = name
    }

}// (6)

/* reference
@Entity
Marks the class as a JPA entity and maps it to a database table.

@Table
Specifies the table name to which the entity is mapped.
If omitted, the table name defaults to the class name.
Declaring it is optional.

@Column
Maps the field to a specific column in the table using the given name.
This annotation is optional if the column name matches the field name.

@Id
Specifies the primary key field of the entity.

@GeneratedValue
Defines how the primary key should be generated (e.g., auto-increment).

No-argument constructor
The entity class must have a no-arg constructor.
It may also define other constructors. — JSR 338

 */
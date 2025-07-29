package ecommerce.study

import jakarta.persistence.*

@Entity // (1)
@Table(name = "station") // (2)
class Station(
    @Column(name = "name", nullable = false) // (3)
    var name: String,
    
    @Id // (4)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // (5)
    val id: Long? = null,
) // (6)

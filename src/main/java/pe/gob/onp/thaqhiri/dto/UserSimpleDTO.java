package pe.gob.onp.thaqhiri.dto;

public class UserSimpleDTO {
    private Integer id;
    private String nombres;

    // Constructor (omitiendo getters/setters para brevedad, pero deberían existir)
    public UserSimpleDTO(Integer id, String nombres) {
        this.id = id;
        this.nombres = nombres;
    }
    
    // Getters y Setters necesarios para serialización JSON
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
}
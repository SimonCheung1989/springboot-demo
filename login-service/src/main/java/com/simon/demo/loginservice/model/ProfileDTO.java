package com.simon.demo.loginservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * ProfileDTO
 */
@Validated
public class ProfileDTO   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("gender")
  private String gender = null;

  @JsonProperty("age")
  private Integer age = null;

  @JsonProperty("id")
  private String id = null;

  @JsonProperty("loginName")
  private String loginName = null;

  @JsonProperty("password")
  private String password = null;

  public ProfileDTO name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  **/

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ProfileDTO gender(String gender) {
    this.gender = gender;
    return this;
  }

  /**
   * Get gender
   * @return gender
  **/

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public ProfileDTO age(Integer age) {
    this.age = age;
    return this;
  }

  /**
   * Get age
   * @return age
  **/

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public ProfileDTO id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  **/

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ProfileDTO loginName(String loginName) {
    this.loginName = loginName;
    return this;
  }

  /**
   * Get loginName
   * @return loginName
  **/

  public String getLoginName() {
    return loginName;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  public ProfileDTO password(String password) {
    this.password = password;
    return this;
  }

  /**
   * Get password
   * @return password
  **/

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProfileDTO profileDTO = (ProfileDTO) o;
    return Objects.equals(this.name, profileDTO.name) &&
        Objects.equals(this.gender, profileDTO.gender) &&
        Objects.equals(this.age, profileDTO.age) &&
        Objects.equals(this.id, profileDTO.id) &&
        Objects.equals(this.loginName, profileDTO.loginName) &&
        Objects.equals(this.password, profileDTO.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, gender, age, id, loginName, password);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProfileDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    gender: ").append(toIndentedString(gender)).append("\n");
    sb.append("    age: ").append(toIndentedString(age)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    loginName: ").append(toIndentedString(loginName)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}


package com.simon.demo.loginservice.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * LoginDTO
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-04-26T10:37:30.070+08:00")

public class LoginDTO   {
  @JsonProperty("loginName")
  private String loginName = null;

  @JsonProperty("password")
  private String password = null;

  public LoginDTO loginName(String loginName) {
    this.loginName = loginName;
    return this;
  }

  /**
   * Get loginName
   * @return loginName
  **/
  @ApiModelProperty(value = "")


  public String getLoginName() {
    return loginName;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  public LoginDTO password(String password) {
    this.password = password;
    return this;
  }

  /**
   * Get password
   * @return password
  **/
  @ApiModelProperty(value = "")


  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoginDTO loginDTO = (LoginDTO) o;
    return Objects.equals(this.loginName, loginDTO.loginName) &&
        Objects.equals(this.password, loginDTO.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(loginName, password);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LoginDTO {\n");
    
    sb.append("    loginName: ").append(toIndentedString(loginName)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}


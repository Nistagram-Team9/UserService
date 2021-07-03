package devops.tim9.userservice.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import devops.tim9.userservice.dto.UserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.JoinColumn;

@Entity
//@Table(catalog = "dbhybridlibrary", name = "books")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

	@Id
	@GeneratedValue
	private Integer id;

	public String name;
	public String surname;
	public String email;
	public String phoneNumber;
	public String sex;
	public String birthDate;
	public String username;
	public String website;
	public String biography;
	public Boolean isPrivate;
	public Boolean canBeTagged;
	public Boolean isActive;
	public String password;
	public Boolean isDisabled;
	public Boolean isAccepted;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "user_authority", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "authority_id", referencedColumnName = "id"))
	@JsonIgnore
	private List<Authority> authorities = new ArrayList<>();

	public User(UserDto userDto, Boolean isAccepted) {
		this.name = userDto.getName();
		this.surname = userDto.getSurname();
		this.email = userDto.getEmail();
		this.phoneNumber = userDto.getPhoneNumber();
		this.sex = userDto.getSex();
		this.birthDate = userDto.getBirthDate();
		this.username = userDto.getUsername();
		this.website = userDto.getWebsite();
		this.biography = userDto.getBiography();
		this.isPrivate = userDto.getIsPrivate();
		this.canBeTagged = userDto.getCanBeTagged();
		this.isActive = userDto.getIsActive();
		this.isDisabled = false;
		this.isAccepted = isAccepted;

	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	public List<Authority> getAuthoitiesList() {
		return authorities;
	}

	@JsonIgnore
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@JsonIgnore
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@JsonIgnore
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@JsonIgnore
	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return this.password;
	}

}

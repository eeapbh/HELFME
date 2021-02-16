package com.web.curation.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Timeline {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int tno;

	private int uno;
	@Column(insertable=false,updatable=false)
	private String tdate;
	private String tcontent;
	private String tcontentSecond;
	
}

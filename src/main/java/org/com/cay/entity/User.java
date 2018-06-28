package org.com.cay.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by Cay on 2018/6/14.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User implements Serializable{

	private String name;
}

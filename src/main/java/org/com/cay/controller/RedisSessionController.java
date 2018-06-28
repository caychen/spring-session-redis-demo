package org.com.cay.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.com.cay.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by Cay on 2018/6/14.
 */
@ResponseBody
@Controller
public class RedisSessionController {

	private final Gson gson = new GsonBuilder().setDateFormat("yyyyMMddHHmmss").create();

	@RequestMapping("/set")
	public String set(HttpSession session, String name) {
		String s = gson.toJson(new User(name));
		session.setAttribute("user", s);
		return s;
	}

	@RequestMapping("/get")
	public String get(HttpSession session){
		User user = gson.fromJson(session.getAttribute("user").toString(), User.class);

		return user.toString() + ",8081";
	}
}

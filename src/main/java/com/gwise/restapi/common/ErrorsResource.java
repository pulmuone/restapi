package com.gwise.restapi.common;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.validation.Errors;

import com.gwise.restapi.index.IndexController;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

public class ErrorsResource extends Resource<Errors> {
	public ErrorsResource(Errors content, Link... links) {
		super(content, links);
		add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
	}
}
package com.gwise.restapi.events;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

//@JsonUnwrapped 사용하지 않고 동일하게 하려면..
public class EventResource extends  Resource<Event> {
	
	public EventResource(Event event, Link... links) {
		super(event, links);
		
		add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
    }

//Event로 결과물을 감싸지 않는다.
//	@JsonUnwrapped
//	private Event event;
//
//	public EventResource(Event event) {
//		this.event = event;
//	}
//
//	public Event getEvent() {
//		return event;
//	}
}

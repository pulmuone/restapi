package com.gwise.restapi.events;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.net.URI;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gwise.restapi.common.ErrorsResource;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {
	private final EventRepository eventRepository;	
	private final ModelMapper modelMapper;
	private final EventValidator eventValidator;
	
	public EventController(EventRepository eventRepository, 
			ModelMapper modelMapper, 
			EventValidator eventValidator) {
		this.eventRepository = eventRepository;
		this.modelMapper = modelMapper;
		this.eventValidator = eventValidator;
	}

	@PostMapping
	public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto,
			Errors errors) {
		//EventDto 제약조건 에러
		//한번에 에러 메시지를 출력하려면 주석처리한다.
		if(errors.hasErrors()) {
			return badRequest(errors);
		}		
		//입력필드 로직체크
		eventValidator.validate(eventDto, errors);
		
		if(errors.hasErrors()) {
			return badRequest(errors);
		}
		
		Event event = modelMapper.map(eventDto, Event.class); 
		//free, offline 필드값 처리
		event.update();
		Event addEvent = this.eventRepository.save(event);

		ControllerLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(addEvent.getId());
		URI createUri = selfLinkBuilder.toUri();
		
		EventResource eventResource = new EventResource(event);
		eventResource.add(linkTo(EventController.class).withRel("query-events"));
		eventResource.add(selfLinkBuilder.withRel("update-event"));

		return ResponseEntity.created(createUri).body(eventResource);
	}

	private ResponseEntity badRequest(Errors errors) {
		//return ResponseEntity.badRequest().body(errors); // .build();
		return ResponseEntity.badRequest().body(new ErrorsResource(errors));
	}
}
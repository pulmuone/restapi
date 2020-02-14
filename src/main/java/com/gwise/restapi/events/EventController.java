package com.gwise.restapi.events;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.net.URI;
import java.util.Optional;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gwise.restapi.common.ErrorsResource;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {
	private final EventRepository eventRepository;
	private final ModelMapper modelMapper;
	private final EventValidator eventValidator;

	public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
		this.eventRepository = eventRepository;
		this.modelMapper = modelMapper;
		this.eventValidator = eventValidator;
	}

	@PutMapping("/{id}")
	public ResponseEntity updateEvent(@PathVariable Integer id, @RequestBody @Valid EventDto eventDto, Errors errors) {
		// Id와 맵핑되는 Event가 없으면 404에러 처리.
		Optional<Event> optionalEvent = this.eventRepository.findById(id);
		if (optionalEvent.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		// 필수입력 체크, 최소값 체크
		if (errors.hasErrors()) {
			return badRequest(errors);
		}
		// 입력항목의 로직을 체크
		this.eventValidator.validate(eventDto, errors);
		if (errors.hasErrors()) {
			return badRequest(errors);
		}
		// 입력받은 EventDto와 DB에서 읽어온 Event를 맵핑
		// 맵핑된 Event를 Save(등록) 한다.
		Event existingEvent = optionalEvent.get();
		this.modelMapper.map(eventDto, existingEvent);
		Event savedEvent = this.eventRepository.save(existingEvent);
		// update된 Event를 EventResource로 Wrapping해서 Response Body에 전달 한다.
		EventResource eventResource = new EventResource(savedEvent);
		return ResponseEntity.ok(eventResource);
	}

	@GetMapping
	public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
		Page<Event> page = this.eventRepository.findAll(pageable);
		PagedResources<Resource<Event>> pagedResource = assembler.toResource(page, event -> new EventResource(event));

		return ResponseEntity.ok(pagedResource);
	}

	@GetMapping("/{id}")
	public ResponseEntity getEvent(@PathVariable Integer id) {
		Optional<Event> optionalEvent = this.eventRepository.findById(id); // 값이 null일 수도 있다.
		if (optionalEvent.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Event event = optionalEvent.get();
		EventResource eventResource = new EventResource(event);
		return ResponseEntity.ok(eventResource);
	}

	@PostMapping
	public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {
		// EventDto 제약조건 에러'
		// 한번에 에러 메시지를 출력하려면 주석처리한다.
		if (errors.hasErrors()) {
			return badRequest(errors);
		}
		// 입력필드 로직체크
		eventValidator.validate(eventDto, errors);

		if (errors.hasErrors()) {
			return badRequest(errors);
		}

		Event event = modelMapper.map(eventDto, Event.class);
		// free, offline 필드값 처리
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
		// return ResponseEntity.badRequest().body(errors); // .build();
		return ResponseEntity.badRequest().body(new ErrorsResource(errors));
	}
}
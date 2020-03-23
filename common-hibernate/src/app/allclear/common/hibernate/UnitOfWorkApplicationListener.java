package app.allclear.common.hibernate;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.*;

/** Jersey server listener that wraps dual Hibernate session factories (writer & reader).
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Provider
public class UnitOfWorkApplicationListener implements ApplicationEventListener
{
	private final ConcurrentMap<ResourceMethod, Optional<UnitOfWork>> methodMap = new ConcurrentHashMap<>();
	private final DualSessionFactory factory;

	public UnitOfWorkApplicationListener(final DualSessionFactory factory)
	{
		this.factory = factory;
	}

	private static class UnitOfWorkEventListener implements RequestEventListener {
		private ConcurrentMap<ResourceMethod, Optional<UnitOfWork>> methodMap;
		private final UnitOfWorkAspect unitOfWorkAspect;

		UnitOfWorkEventListener(ConcurrentMap<ResourceMethod, Optional<UnitOfWork>> methodMap,
				final DualSessionFactory factory)
		{
			this.methodMap = methodMap;
			this.unitOfWorkAspect = new UnitOfWorkAspect(factory);
		}

		@Override
		public void onEvent(RequestEvent event)
		{
			var eventType = event.getType();
			if (eventType == RequestEvent.Type.RESOURCE_METHOD_START)
			{
				var unitOfWork = methodMap.computeIfAbsent(event.getUriInfo()
						.getMatchedResourceMethod(), UnitOfWorkEventListener::registerUnitOfWorkAnnotations);
				unitOfWorkAspect.beforeStart(unitOfWork.orElse(null));
			}
			else if (eventType == RequestEvent.Type.RESP_FILTERS_START)
			{
				try { unitOfWorkAspect.afterEnd(); }
				catch (Exception e) { throw new MappableException(e); }
			}
			else if (eventType == RequestEvent.Type.ON_EXCEPTION)
				unitOfWorkAspect.onError();
			else if (eventType == RequestEvent.Type.FINISHED)
				unitOfWorkAspect.onFinish();
		}

		private static Optional<UnitOfWork> registerUnitOfWorkAnnotations(ResourceMethod method)
		{
			var annotation = method.getInvocable().getDefinitionMethod().getAnnotation(UnitOfWork.class);
			if (annotation == null)
				annotation = method.getInvocable().getHandlingMethod().getAnnotation(UnitOfWork.class);

			return Optional.ofNullable(annotation);
		}
	}

	@Override
	public void onEvent(ApplicationEvent event) {}

	@Override
	public RequestEventListener onRequest(final RequestEvent event)
	{
		return new UnitOfWorkEventListener(methodMap, factory);
	}
}

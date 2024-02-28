package com.ef.mediaroutingengine.routing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ef.cim.objectmodel.CCUser;
import com.ef.cim.objectmodel.MediaRoutingDomain;
import com.ef.cim.objectmodel.PrecisionQueueEntity;
import com.ef.cim.objectmodel.RoutingAttribute;
import com.ef.cim.objectmodel.RoutingAttributeType;
import com.ef.mediaroutingengine.global.exceptions.NotFoundException;
import com.ef.mediaroutingengine.routing.dto.PrecisionQueueRequestBody;
import com.ef.mediaroutingengine.routing.dto.RoutingAttributeDeleteConflictResponse;
import com.ef.mediaroutingengine.routing.pool.RoutingAttributesPool;
import com.ef.mediaroutingengine.routing.repository.AgentsRepository;
import com.ef.mediaroutingengine.routing.repository.PrecisionQueueRepository;
import com.ef.mediaroutingengine.routing.repository.RoutingAttributeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class RoutingAttributesServiceImplTest {

    @Mock
    private RoutingAttributeRepository repository;

    @Mock
    private RoutingAttributesPool routingAttributesPool;

    @Mock
    private PrecisionQueueRepository precisionQueueRepository;

    @Mock
    private AgentsRepository agentsRepository;

    private RoutingAttributesServiceImpl routingAttributesService;

    @BeforeEach
    void setUp() {
        this.routingAttributesService =
                new RoutingAttributesServiceImpl(repository, routingAttributesPool, precisionQueueRepository,
                        agentsRepository);
    }


    @Test
    void testCreate_when_routingAttributeIsCreated() {
        RoutingAttribute routingAttribute = getRoutingAttributeRequest();

        doReturn(routingAttribute).when(this.repository).insert((RoutingAttribute) any());

        RoutingAttribute returnedValue = routingAttributesService.create(routingAttribute);

        ArgumentCaptor<RoutingAttribute> routingAttributeArgumentCaptor =
                ArgumentCaptor.forClass(RoutingAttribute.class);
        verify(this.repository, times(1)).insert(routingAttribute);
        verify(this.routingAttributesPool, times(1)).insert(routingAttributeArgumentCaptor.capture());
        assertEquals(routingAttribute.getId(), returnedValue.getId());

    }

    @Test
    void testUpdatePrecisionQueue_when_UpdateIsSuccessful() {
        RoutingAttribute routingAttribute = getRoutingAttributeRequest();
        String routingId = UUID.randomUUID().toString();

        PrecisionQueueEntity entity = mock(PrecisionQueueEntity.class);
        List<PrecisionQueueEntity> precisionQueueEntities = new ArrayList<>();
        precisionQueueEntities.add(entity);

        doReturn(precisionQueueEntities).when(this.precisionQueueRepository).findByRoutingAttributeId(routingId);

        routingAttributesService.updatePrecisionQueues(routingAttribute, routingId);

        verify(this.precisionQueueRepository).saveAll(any());
    }

    private RoutingAttribute getRoutingAttributeRequest() {
        RoutingAttribute routingAttribute = new RoutingAttribute();
        routingAttribute.setId(UUID.randomUUID().toString());
        routingAttribute.setName("test");
        routingAttribute.setType(RoutingAttributeType.BOOLEAN);
        return routingAttribute;
    }

    private PrecisionQueueRequestBody getPrecisionQueueRequest() {
        PrecisionQueueRequestBody precisionQueueRequestBody = new PrecisionQueueRequestBody();
        precisionQueueRequestBody.setId(UUID.randomUUID().toString());
        precisionQueueRequestBody.setName("test");
        precisionQueueRequestBody.setMrd(getMRD());
        precisionQueueRequestBody.setServiceLevelThreshold(3);
        return precisionQueueRequestBody;
    }

    private MediaRoutingDomain getMRD() {
        MediaRoutingDomain mediaRoutingDomain = new MediaRoutingDomain();
        mediaRoutingDomain.setId(UUID.randomUUID().toString());
        mediaRoutingDomain.setName("chat");
        return mediaRoutingDomain;
    }

    @Nested
    class retrieveAgentsWithAssociatedRoutingAttributes {
        @Test
        void should_return_listOfAgents_withAssociatedRoutingAttributes() {
            RoutingAttribute routingAttribute = getRoutingAttributeRequest();

            when(routingAttributesPool.existsById(routingAttribute.getId())).thenReturn(true);
            when(agentsRepository.findByRoutingAttributeId(routingAttribute.getId())).thenReturn(List.of(new CCUser()));
            assertEquals(1,
                    routingAttributesService.retrieveAgentsWithAssociatedRoutingAttributes(List.of(routingAttribute))
                            .size());
            assertFalse(
                    routingAttributesService.retrieveAgentsWithAssociatedRoutingAttributes(List.of(routingAttribute))
                            .isEmpty());
        }

        @Test
        void should_throw_NOT_FOUND_whenProvidedRoutingAttributeDoesNotExists() {
            RoutingAttribute routingAttribute = getRoutingAttributeRequest();

            when(routingAttributesPool.existsById(routingAttribute.getId())).thenReturn(false);
            assertThrows(NotFoundException.class,
                    () -> routingAttributesService.retrieveAgentsWithAssociatedRoutingAttributes(
                            List.of(routingAttribute)));
        }
    }

    @Nested
    @DisplayName("Update method tests")
    class UpdateTest {

        @Test
        void when_routingAttributeDoesNotExistInRepository() {
            RoutingAttribute routingAttribute = getRoutingAttributeRequest();
            String routingId = UUID.randomUUID().toString();
            assertThrows(NotFoundException.class, () -> routingAttributesService.update(routingAttribute, routingId));
        }

        @Test
        void when_routingAttributeExistInRepository() {
            RoutingAttribute routingAttribute = getRoutingAttributeRequest();
            String routingId = UUID.randomUUID().toString();

            RoutingAttributesServiceImpl spy = Mockito.spy(routingAttributesService);

            doReturn(true).when(repository).existsById(routingId);
            doReturn(routingAttribute).when(repository).save(any());

            RoutingAttribute returnedValue = spy.update(routingAttribute, routingId);

            verify(routingAttributesPool, times(1)).update(routingAttribute);
            verify(repository, times(1)).save(routingAttribute);
            assertEquals(routingAttribute.getId(), returnedValue.getId());

        }
    }

    @Nested
    @DisplayName("Delete method tests")
    class DeleteTests {
        @Test
        void when_routingAttributeIsDeleted() {
            String routingAttributeId = UUID.randomUUID().toString();

            RoutingAttributesServiceImpl spy = Mockito.spy(routingAttributesService);

            doReturn(true).when(repository).existsById(routingAttributeId);

            RoutingAttributeDeleteConflictResponse response = spy.delete(routingAttributeId);

            verify(routingAttributesPool, times(1)).deleteById(any());
            verify(repository, times(1)).deleteById(any());
            assertNull(response);

        }

        @Test
        void when_queuesOrAgentsAreAssociated() {
            String id = UUID.randomUUID().toString();
            PrecisionQueueRequestBody requestBody = getPrecisionQueueRequest();

            PrecisionQueueEntity entity = new PrecisionQueueEntity();
            entity.setId(id);
            entity.setName(requestBody.getName());
            entity.setMrd(requestBody.getMrd());
            entity.setServiceLevelType(requestBody.getServiceLevelType());
            entity.setServiceLevelThreshold(requestBody.getServiceLevelThreshold());

            List<PrecisionQueueEntity> precisionQueueEntities = new ArrayList<>();
            precisionQueueEntities.add(entity);

            RoutingAttributeDeleteConflictResponse response = new RoutingAttributeDeleteConflictResponse();
            response.setPrecisionQueueEntities(precisionQueueEntities);

            RoutingAttributesServiceImpl spy = Mockito.spy(routingAttributesService);

            doReturn(true).when(repository).existsById(id);
            doReturn(precisionQueueEntities).when(precisionQueueRepository).findByRoutingAttributeId(id);

            RoutingAttributeDeleteConflictResponse responseReturned = spy.delete(id);

            assertEquals(response.getPrecisionQueueEntities(), responseReturned.getPrecisionQueueEntities());
        }

    }

    @Nested
    @DisplayName("UpdateAgents method tests")
    class UpdateAgentsTest {

        @Test
        void when_agentDoesNotExistInRepository() {
            RoutingAttribute routingAttribute = getRoutingAttributeRequest();
            String routingId = UUID.randomUUID().toString();

            assertFalse(routingAttributesService.updateAgents(routingAttribute, routingId));
        }

        @Test
        void when_updateIsSuccessful() {
            RoutingAttribute routingAttribute = getRoutingAttributeRequest();
            String routingId = UUID.randomUUID().toString();

            CCUser agent = mock(CCUser.class);
            List<CCUser> agents = new ArrayList<>();
            agents.add(agent);
            doReturn(agents).when(agentsRepository).findByRoutingAttributeId(routingId);

            routingAttributesService.updateAgents(routingAttribute, routingId);

            verify(agentsRepository).saveAll(any());
            assertTrue(routingAttributesService.updateAgents(routingAttribute, routingId));
        }

    }
}
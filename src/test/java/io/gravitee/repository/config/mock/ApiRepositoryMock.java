/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.repository.config.mock;

import io.gravitee.repository.management.api.ApiRepository;
import io.gravitee.repository.management.api.search.ApiCriteria;
import io.gravitee.repository.management.api.search.ApiFieldExclusionFilter;
import io.gravitee.repository.management.api.search.builder.PageableBuilder;
import io.gravitee.repository.management.model.Api;
import io.gravitee.repository.management.model.LifecycleState;
import io.gravitee.repository.management.model.Visibility;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.util.collections.Sets;

import static io.gravitee.repository.management.model.LifecycleState.STARTED;
import static io.gravitee.repository.management.model.LifecycleState.STOPPED;
import static io.gravitee.repository.management.model.Visibility.PUBLIC;
import static io.gravitee.repository.utils.DateUtils.parse;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ApiRepositoryMock extends AbstractRepositoryMock<ApiRepository> {

    public ApiRepositoryMock() {
        super(ApiRepository.class);
    }

    @Override
    void prepare(ApiRepository apiRepository) throws Exception {
        final Api apiToDelete = mock(Api.class);
        when(apiToDelete.getId()).thenReturn("api-to-delete");

        final Api apiToUpdate = mock(Api.class);
        when(apiToUpdate.getId()).thenReturn("api-to-update");
        when(apiToUpdate.getName()).thenReturn("api-to-update");
        final Api apiUpdated = mock(Api.class);
        when(apiUpdated.getName()).thenReturn("New API name");
        when(apiUpdated.getDescription()).thenReturn("New description");
        when(apiUpdated.getViews()).thenReturn(Sets.newSet("view1", "view2"));
        when(apiUpdated.getDefinition()).thenReturn("New definition");
        when(apiUpdated.getDeployedAt()).thenReturn(parse("11/02/2016"));
        when(apiUpdated.getGroups()).thenReturn(singleton("New group"));
        when(apiUpdated.getLifecycleState()).thenReturn(STARTED);
        when(apiUpdated.getPicture()).thenReturn("New picture");
        when(apiUpdated.getCreatedAt()).thenReturn(parse("11/02/2016"));
        when(apiUpdated.getUpdatedAt()).thenReturn(parse("13/11/2016"));
        when(apiUpdated.getVersion()).thenReturn("New version");
        when(apiUpdated.getVisibility()).thenReturn(Visibility.PRIVATE);

        when(apiRepository.findById("api-to-update")).thenReturn(of(apiToUpdate), of(apiUpdated));

        when(apiRepository.findById("api-to-delete")).thenReturn(of(apiToDelete), empty());

        when(apiRepository.findById("findByNameMissing")).thenReturn(empty());

        final Api newApi = mock(Api.class);
        when(newApi.getVersion()).thenReturn("1");
        when(newApi.getLifecycleState()).thenReturn(LifecycleState.STOPPED);
        when(newApi.getVisibility()).thenReturn(Visibility.PRIVATE);
        when(newApi.getDefinition()).thenReturn("{}");
        when(newApi.getCreatedAt()).thenReturn(parse("11/02/2016"));
        when(newApi.getUpdatedAt()).thenReturn(parse("12/02/2016"));
        when(apiRepository.findById("sample-new")).thenReturn(of(newApi));

        final Api groupedApi = mock(Api.class);
        when(groupedApi.getGroups()).thenReturn(singleton("api-group"));
        when(groupedApi.getId()).thenReturn("grouped-api");
        when(apiRepository.findById("grouped-api")).thenReturn(of(groupedApi));

        final Api apiToFindById = mock(Api.class);
        when(apiToFindById.getId()).thenReturn("api-to-findById");
        when(apiToFindById.getVersion()).thenReturn("1");
        when(apiToFindById.getName()).thenReturn("api-to-findById");
        when(apiToFindById.getLifecycleState()).thenReturn(LifecycleState.STOPPED);
        when(apiToFindById.getVisibility()).thenReturn(PUBLIC);
        when(apiToFindById.getDefinition()).thenReturn(null);
        when(apiToFindById.getCreatedAt()).thenReturn(parse("11/02/2016"));
        when(apiToFindById.getUpdatedAt()).thenReturn(parse("12/02/2016"));
        when(apiToFindById.getLabels()).thenReturn(asList("label 1", "label 2"));
        when(apiRepository.findById("api-to-findById")).thenReturn(of(apiToFindById));

        when(apiRepository.search(null)).thenReturn(asList(mock(Api.class), mock(Api.class), mock(Api.class), mock(Api.class)));

        when(apiRepository.search(new ApiCriteria.Builder().ids("api-to-delete", "api-to-update", "unknown").build())).
                thenReturn(asList(apiToUpdate, apiToDelete));

        when(apiRepository.update(argThat(new ArgumentMatcher<Api>() {
            @Override
            public boolean matches(Object o) {
                return o == null || (o instanceof Api && ((Api) o).getId().equals("unknown"));
            }
        }))).thenThrow(new IllegalStateException());

        when(apiRepository.search(new ApiCriteria.Builder().name("api-to-findById").build())).thenReturn(singletonList(apiToFindById));
        when(apiRepository.search(new ApiCriteria.Builder().view("my-view").build())).thenReturn(singletonList(apiToFindById));
        when(apiRepository.search(new ApiCriteria.Builder().name("api-to-findById").version("1").build())).thenReturn(singletonList(apiToFindById));
        when(apiRepository.search(new ApiCriteria.Builder().name("api-to-findById").version("1").build(),
                new ApiFieldExclusionFilter.Builder().excludeDefinition().build())).thenReturn(singletonList(apiToFindById));
        when(apiRepository.search(new ApiCriteria.Builder().groups("api-group", "unknown").build())).thenReturn(singletonList(groupedApi));
        when(apiRepository.search(new ApiCriteria.Builder().version("1").build())).thenReturn(asList(apiToFindById,
                groupedApi, apiToDelete, apiToUpdate));
        when(apiRepository.search(new ApiCriteria.Builder().label("label 1").build())).thenReturn(singletonList(apiToFindById));
        when(apiRepository.search(new ApiCriteria.Builder().state(STOPPED).build())).thenReturn(asList(apiToFindById,
                groupedApi, apiToDelete, apiToUpdate));
        when(apiRepository.search(new ApiCriteria.Builder().visibility(PUBLIC).build())).thenReturn(asList(apiToFindById,
                groupedApi));

        when(apiRepository.search(
                new ApiCriteria.Builder().version("1").build(),
                new PageableBuilder().pageNumber(0).pageSize(2).build())).thenReturn(
                new io.gravitee.common.data.domain.Page<>(asList(apiToDelete, apiToFindById), 0, 2, 4));
        when(apiRepository.search(
                new ApiCriteria.Builder().version("1").build(),
                new PageableBuilder().pageNumber(1).pageSize(2).build())).thenReturn(
                new io.gravitee.common.data.domain.Page<>(asList(apiToUpdate, groupedApi), 1, 2, 4));
        when(apiRepository.search(
                new ApiCriteria.Builder().version("1").build(), new PageableBuilder().build())).thenReturn(
                new io.gravitee.common.data.domain.Page<>(asList(apiToDelete, apiToFindById, apiToUpdate, groupedApi), 0, 4, 4));

    }
}
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as showApi from '../../../api/showApi'
import { showKeys } from '../showKeys'

export function useShowLibrary() {
  return useQuery({
    queryKey: showKeys.library(),
    queryFn: showApi.listLibrary,
  })
}

export function useShowSearch(query: string) {
  return useQuery({
    queryKey: showKeys.search(query),
    queryFn: () => showApi.searchShows(query),
    enabled: query.trim().length >= 2,
  })
}

export function useShowDetail(id: string) {
  return useQuery({
    queryKey: showKeys.detail(id),
    queryFn: () => showApi.getShow(id),
    enabled: !!id,
  })
}

export function useAddShow() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (tvmazeId: number) => showApi.addShow(tvmazeId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: showKeys.library() })
    },
  })
}

export function useUpdateLibraryStatus(showId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (libraryStatus: showApi.PatchableLibraryStatus) =>
      showApi.updateLibraryStatus(showId, libraryStatus),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: showKeys.library() })
      queryClient.invalidateQueries({ queryKey: showKeys.detail(showId) })
    },
  })
}

export function useRemoveShow() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (showId: string) => showApi.removeShow(showId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: showKeys.library() })
    },
  })
}

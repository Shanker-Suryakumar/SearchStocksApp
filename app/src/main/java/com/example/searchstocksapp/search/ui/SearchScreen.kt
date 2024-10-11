import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.searchstocksapp.search.rest.dto.SearchResptDto
import com.example.searchstocksapp.search.viewmodel.SearchViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.core.component.getScopeId

@Composable
fun SearchScreenMain(modifier: Modifier, activity: Activity) {
    val searchViewModel = getViewModel<SearchViewModel>()

    val isSearching by searchViewModel.isSearching.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsStateWithLifecycle()

    Column(modifier = modifier) {
        SearchScreenView(
            isSearching = isSearching,
            searchText = searchViewModel.searchText,
            searchResults = searchResults,
            onSearchTextChange = { searchViewModel.onSearchTextChange(it) }
        )
    }

    BackHandler {
        searchViewModel.onBackClick(activity = activity)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenView(
    isSearching: Boolean,
    searchText: String,
    searchResults: List<SearchResptDto>,
    onSearchTextChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    SearchBar(
        query = searchText,
        onQueryChange = onSearchTextChange,
        onSearch = { keyboardController?.hide() },
        placeholder = {
            Text(text = "Search Stocks")
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "Search icon"
            )
        },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                IconButton(onClick = { onSearchTextChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        content = {
            if (searchResults.isEmpty()) {
                SearchListEmptyState()
            } else {
                if (isSearching) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(space = 32.dp),
                        contentPadding = PaddingValues(all = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = searchResults.size,
                            key = { index -> searchResults[index].getScopeId() },
                            itemContent = { index ->
                                val stock = searchResults[index]
                                SearchListItem(stock = stock)
                            }
                        )
                    }
                }
            }
        },
        active = true,
        onActiveChange = {},
        tonalElevation = 0.dp
    )
}

@Composable
fun SearchListItem(
    stock: SearchResptDto,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        stock.ticker?.let { Text(text = it) }
        stock.name?.let { Text(text = it) }
        stock.currentPrice?.let { Text(text = it.toString()) }
    }
}

@Composable
fun SearchListEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "No stock found",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = "Try adjusting your search",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
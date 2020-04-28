import kotlinext.js.jsObject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.h1
import react.dom.li
import react.dom.ul

val scope = MainScope()

val App = functionalComponent<RProps> { _ ->
    val (shoppingList, setShoppingList) = useState(emptyList<ShoppingListItem>())

    useEffect(dependencies = listOf()) {
        scope.launch {
            setShoppingList(getShoppingList())
        }
    }

    h1 {
        +"Full-Stack Shopping List"
    }
    // Shopping list
    ul {
        shoppingList.sortedByDescending(ShoppingListItem::priority).forEach { item ->
            li {
                // Clicking an item will issue the delete call, and subsequently load the updated shopping list
                attrs.onClickFunction = {
                    scope.launch {
                        deleteShoppingListItem(item)
                        setShoppingList(getShoppingList())
                    }
                }
                // Display item priority and description
                +"[${item.priority}] ${item.desc} "
            }
        }
    }

    child(
        // New Item text box
        functionalComponent = InputComponent,
        props = jsObject {
            onSubmit = { input ->
                // Replace number of ! into priority value submitted to the backend
                val cartItem = ShoppingListItem(input.replace("!", ""), input.count { it == '!' })
                scope.launch {
                    addShoppingListItem(cartItem)
                    setShoppingList(getShoppingList())
                }
            }
        }
    )
}
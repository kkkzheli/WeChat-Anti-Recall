package kkkzheli.antirecall.wechat.ui.compose.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import kkkzheli.antirecall.wechat.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
private fun getDayAbbreviations(): List<String> {
    return LocalContext.current.resources.getStringArray(R.array.weekday_abbr).toList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    currentStart: LocalDate? = null,
    currentEnd: LocalDate? = null,
    onConfirm: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = LocalDate.now()
    var tempStart by remember { mutableStateOf(currentStart ?: today) }
    var tempEnd by remember { mutableStateOf(currentEnd ?: today.plusDays(1)) }
    var startYM by remember { mutableStateOf(YearMonth.from(tempStart)) }
    var endYM by remember { mutableStateOf(YearMonth.from(tempEnd)) }

    fun commit() {
        onConfirm(tempStart, tempEnd)
        onDismiss()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(text = stringResource(R.string.filter_date_start), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 4.dp))
            MiniCalendar(
                yearMonth = startYM, selectedDate = tempStart, maxDate = tempEnd,
                onDateSelected = { d -> tempStart = d; startYM = YearMonth.from(d) },
                onPrevMonth = { startYM = startYM.minusMonths(1) },
                onNextMonth = { startYM = startYM.plusMonths(1) },
                highlightColor = MaterialTheme.colorScheme.primaryContainer,
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Text(text = stringResource(R.string.filter_date_end), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 4.dp))
            MiniCalendar(
                yearMonth = endYM, selectedDate = tempEnd, minDate = tempStart,
                onDateSelected = { d -> tempEnd = d; endYM = YearMonth.from(d) },
                onPrevMonth = { endYM = endYM.minusMonths(1) },
                onNextMonth = { endYM = endYM.plusMonths(1) },
                highlightColor = MaterialTheme.colorScheme.secondaryContainer,
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.filter_date_cancel)) }
                TextButton(onClick = ::commit) { Text(stringResource(R.string.filter_date_confirm)) }
            }
        }
    }
}

@Composable
private fun MiniCalendar(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    highlightColor: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevMonth) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.filter_back_desc)) }
            Text(text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            IconButton(onClick = onNextMonth) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.next_month)) }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (abbrev in getDayAbbreviations()) {
                Text(text = abbrev, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f).defaultMinSize(minHeight = 28.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val days = buildCalendarDays(yearMonth)
        LazyVerticalGrid(columns = GridCells.Fixed(7), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 2.dp)) {
            items(days) { cell ->
                Box(
                    modifier = Modifier.defaultMinSize(minHeight = 32.dp, minWidth = 32.dp)
                        .then(if (!cell.isEmpty) Modifier.clickable(enabled = !isDisabled(cell.date, selectedDate, minDate, maxDate)) { onDateSelected(cell.date) } else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    if (cell.isEmpty) return@Box
                    val isSelected = cell.date == selectedDate
                    val bg = if (isSelected) highlightColor else Color.Transparent
                    val fg = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else if (isDisabled(cell.date, selectedDate, minDate, maxDate)) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.onSurface
                    Surface(shape = RoundedCornerShape(50.dp), color = bg, modifier = Modifier.defaultMinSize(minHeight = 28.dp, minWidth = 28.dp)) {
                        Text(text = cell.date.dayOfMonth.toString(), fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = fg, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
    }
}

data class Cell(val isEmpty: Boolean, val date: LocalDate = LocalDate.now())

private fun buildCalendarDays(ym: YearMonth): List<Cell> {
    val result = mutableListOf<Cell>()
    val offset = (ym.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    for (i in 0 until offset) result.add(Cell(true))
    for (d in 1..ym.lengthOfMonth()) result.add(Cell(false, ym.atDay(d)))
    while (result.size % 7 != 0) result.add(Cell(true))
    return result
}

private fun isDisabled(date: LocalDate, selected: LocalDate, minDate: LocalDate?, maxDate: LocalDate?): Boolean {
    return (minDate != null && date.isBefore(minDate)) || (maxDate != null && date.isAfter(maxDate))
}

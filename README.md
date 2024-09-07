# TV Guide Component

This repository contains the implementation of a TV Guide component built using Jetpack Compose for
Android TV applications. The component is designed to provide a user-friendly interface for browsing
TV channels and their corresponding events.

### Android
![Android Demo](/android.png)

### Dekstop
![Desktop Demo](/desktop.png)

## Features

- Display a list of TV channels.
- Show events for each channel.
- Allow users to scroll and select events.
- Handle focus changes and keyboard interactions.
- Responsive design to adapt to different screen sizes.

## Getting Started

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/yourusername/tv-guide-component.git
   cd tv-guide-component
   ```
2. Open the project in Android Studio.
3. Sync the project to download the dependencies.

### Usage

To integrate the TV Guide component into your application use it like any other Composable function:

```kotlin

TvGuide(
    startTime = startTime,
    endTime = stopTime,
    hoursInViewport = 2.hours,
    fastScroll = true, // If true it will scroll fast after 2 seconds long press
    timeSpacing = 30.minutes, // Spacing between time cells in time bar
    scrollToNow = true, // Whether to scroll to the current time when the guide is displayed.
    onEventSelected = { channel: Int, event: Int ->
        // Do something on event selection 
    },
    onEventClicked = { channel: Int, event: Int ->
        // Do something on event click
    }
) {


    CurrentDay(
        modifier = Modifier
    ) { time: Long ->
        // Draw Current Day
    }

    Timebar(
        height = 20.dp, modifier = Modifier
    ) {

        TimeCell(modifier = Modifier) { time: Long ->
            // Draw time cell
        }
    }

    Channels(
        width = 250.dp,
        height = 30.dp,
        itemCount = provider.channelCount, // Number of channels to display
        selectionScale = 2f, // Selected channel's row scale
        modifier = Modifier
    ) { channel: Int, isSelected: Boolean ->
        ChannelCell(
            modifier = Modifier
        ) {
            // Single channel cell's layout
        }

        Events(
            modifier = Modifier,
            events = provider.eventsOfChannel(channel) // list of events for channel
        ) { event: Event, isEventSelected: Boolean ->
            EventCell(
                modifier = Modifier
            ) {
                // Event cell's layout
            }

        }
    }

    Now(modifier = Modifier) {
        // Draw current time indicator line
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = Color.Red,
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 3f
            )
        }
    }

}

```

# License

This project is licensed under the GNU General Public License v3.0 - see the [License](/LICENSE)file
for details.

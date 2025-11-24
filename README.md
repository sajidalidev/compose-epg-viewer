# TV Guide Component

This repository contains the implementation of a TV Guide component built using Compose Multiplatform. It supports Android, Desktop, and iOS. The component is designed to provide a user-friendly interface for browsing TV channels and their corresponding events.

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
- **Compose Multiplatform** support (Android, Desktop, iOS).

## Getting Started

### Installation

Add the dependency to your module's `build.gradle.kts`:

```kotlin
implementation("dev.sajidali:tvguide:0.0.1")
```

Alternatively, you can clone the repository:

1. Clone the repository:

   ```sh
   git clone https://github.com/yourusername/tv-guide-component.git
   cd tv-guide-component
   ```
2. Open the project in your IDE (Android Studio).
3. Sync the project to download the dependencies.

### Usage

To integrate the TV Guide component into your application, use the `TvGuide` composable. You need to create a state using `rememberGuideState` and pass it to the component.

```kotlin
// Create the state
val state = rememberGuideState()

TvGuide(
    state = state,
    modifier = Modifier.fillMaxSize()
) {
    // Define the Header
    Header(
        modifier = Modifier.background(Color.LightGray)
    ) {
        // Current Day Indicator
        CurrentDay(
            width = 250.dp,
            modifier = Modifier
        ) { time: Long ->
            // Draw Current Day content
        }

        // Timebar
        Timebar(
            modifier = Modifier
        ) { time: Long ->
            // Draw time cell content
            Text(text = time.formatToPattern("HH:mm"))
        }
    }

    // Define Channels and Events
    Channels(
        width = 250.dp,
        itemsCount = provider.channelCount, // Number of channels
        modifier = Modifier
    ) { isSelected ->
        // 'position' is available in this scope (ChannelRowScope)
        
        ChannelRow(
            modifier = Modifier.height(60.dp)
        ) {
            // Draw Channel Cell content (Icon, Name, etc.)
            Text(text = "Channel $position")

            // Define Events for this channel
            Events(
                modifier = Modifier,
                events = provider.eventsOfChannel(position) // List<Event>
            ) { event, isEventSelected ->
                
                EventCell(
                    event = event,
                    modifier = Modifier
                ) {
                    // Draw Event content
                    Text(text = event.title)
                }
            }
        }
    }

    // Current Time Indicator
    Now(modifier = Modifier) {
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

This project is licensed under the Apache License 2.0.

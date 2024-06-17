<template>
  <section v-if="quotesSection" class="quotes" role="region" aria-label="The Quotes">
    <SlideShow
      slides-wrapper-classes="quotes__sliderwrapper"
      slides-container-classes="quotes__slides"
      arrows-container-classes="quotes__arrows"
      left-arrow-classes="quotes__arrow quotes__arrow--left"
      right-arrow-classes="quotes__arrow quotes__arrow--right"
      :slide-count="cards.length"
      :initial-center-slide="initialCenterSlide"
      @update:currentSlide="updateCurrentSlide"
      :slide-width="slideWidth"
    >
      <div v-if="cards.length % 2 === 0" role="listitem" class="quotes__slide">
        <div class="quotes__slide-videoContainer"></div>
      </div>
      <div v-for="(card, index) in cards" :key="index" role="listitem" class="quotes__slide">
        <div
          :class="{
            'quotes__slide-video--zoom-out': currentSlide !== index - initialCenterSlide + 1,
            'quotes__slide-video': true,
          }"
        >
          <div :id="'video-' + card.icon" class="cookieconsent-optin-marketing"></div>
          <div
            class="quotes__slide-thumbnail-overlay cookieconsent-optin-marketing"
            :style="{ backgroundImage: `url(https://img.youtube.com/vi/${card.icon}/maxresdefault.jpg)` }"
            v-show="currentSlide === index - initialCenterSlide + 1 ? showThumbnail : true"
          >
            <div
              class="quotes__play-icon"
              @click="toggleThumbnailAndPlayVideo(index - initialCenterSlide + 1, card.icon)"
            >
              <div class="quotes__play-arrow"></div>
            </div>
          </div>
          <div
            class="quotes__slide-thumbnail-overlay cookieconsent-optout-marketing"
            :style="{ backgroundImage: `url(https://img.youtube.com/vi/${card.icon}/maxresdefault.jpg)` }"
            v-show="currentSlide === index - initialCenterSlide + 1 ? showThumbnail : true"
          >
            <div class="quotes__play-icon" @click="renewCookieConsent">
              <div class="quotes__play-arrow"></div>
            </div>
          </div>
        </div>
      </div>
    </SlideShow>
    <transition name="fade" mode="out-in">
      <p class="quotes__slide-text" :key="currentCardInfo.date">{{ currentCardInfo.date }}</p>
    </transition>
    <transition name="fade" mode="out-in">
      <h3 class="quotes__slide-title" :key="currentCardInfo.title">
        {{ currentCardInfo.title }}
        <span v-for="(titleSegment, index) of currentCardInfo.text?.split('\n') ?? []" :key="index">
          {{ titleSegment }}
        </span>
      </h3>
    </transition>
    <ButtonComponent
      :label="quotesSection.text[0]"
      buttonType="button-component quotes__button"
      ariaLabel="Start your Dataland Journey"
      @click="register"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, watch, inject } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { registerAndRedirectToSearchPage } from '@/utils/KeycloakUtils';
import type Keycloak from 'keycloak-js';
import type { Section } from '@/types/ContentTypes';
import ButtonComponent from '@/components/resources/newLandingPage/ButtonComponent.vue';
import SlideShow from '@/components/general/SlideShow.vue';

interface YouTubeEvent {
  target?: {
    playVideo: () => void;
    pauseVideo: () => void;
  };
}
declare global {
  interface Window {
    Cookiebot?: {
      renew: () => void;
    };
    YT: {
      Player: new (
        elementId: string,
        opts: {
          videoId: string;
          playerVars?: {
            rel: number;
          };
          events: {
            onReady: (event: YouTubeEvent) => void;
          };
        }
      ) => {
        playVideo: () => void;
        pauseVideo: () => void;
        destroy: () => void;
      };
    };
    onYouTubeIframeAPIReady: () => void;
  }
}

const ytPlayers = ref<Map<string, { playVideo: () => void; pauseVideo: () => void; destroy: () => void }>>(new Map());

const { sections } = defineProps<{ sections?: Section[] }>();
const quotesSection = computed(() => sections?.find((section) => section.title === 'Quotes'));
const cards = computed(() => quotesSection.value?.cards ?? []);
const currentSlide = ref(1);
const slideWidth = ref(640);
const showThumbnail = ref(true);
const initialCenterSlide = computed(() => {
  if (cards.value.length % 2 === 0) {
    return cards.value.length / 2 - 1;
  } else {
    return cards.value.length / 2 - 0.5;
  }
});

const currentCardInfo = computed(() => {
  const card = cards.value[currentSlide.value + initialCenterSlide.value - 1];
  return {
    date: card?.date,
    title: card?.title,
    text: card?.text,
  };
});

const renewCookieConsent = (): void => {
  window.Cookiebot?.renew();
};

const pauseAllVideos = (): void => {
  ytPlayers.value.forEach((player) => {
    player.pauseVideo();
  });
};

const toggleThumbnailAndPlayVideo = (slideIndex: number, videoId?: string): void => {
  if (videoId && currentSlide.value === slideIndex) {
    showThumbnail.value = !showThumbnail.value;
    const player = ytPlayers.value.get(videoId);
    if (player && !showThumbnail.value) {
      player.playVideo();
    }
  }
};

const updateCurrentSlide = (newSlide: number): void => {
  currentSlide.value = newSlide + 1;
  pauseAllVideos();
};
watch(
  () => currentSlide.value,
  () => {
    pauseAllVideos();
    showThumbnail.value = true;
  }
);

const updateSlideWidth = (): void => {
  slideWidth.value = window.innerWidth > 768 ? 640 : 323;
};

onMounted(() => {
  window.addEventListener('resize', updateSlideWidth);
  updateSlideWidth();
  const firstScriptTag = document.querySelector('script');
  if (firstScriptTag?.parentNode) {
    const tag = document.createElement('script');
    tag.src = 'https://www.youtube.com/iframe_api';
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
  }
  window.onYouTubeIframeAPIReady = (): void => {
    cards.value.forEach((card) => {
      if (!card.icon) return;

      const player = new window.YT.Player(`video-${card.icon}`, {
        videoId: card.icon,
        playerVars: {
          rel: 0,
        },
        events: {
          onReady: (event): void => {
            event.target?.pauseVideo();
          },
        },
      });
      ytPlayers.value.set(card.icon, player);
    });
  };
});

onUnmounted(() => {
  window.removeEventListener('resize', updateSlideWidth);
  ytPlayers.value.forEach((player) => {
    player.destroy();
  });
});

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
/**
 * Sends the user to the keycloak register page (if not authenticated already)
 */
const register = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (!keycloak.authenticated) {
        registerAndRedirectToSearchPage(keycloak);
      }
    })
    .catch((error) => console.log(error));
};
</script>

<style lang="scss">
.quotes {
  margin: 0 auto 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  gap: 40px;
  &__slides {
    display: flex;
    transition: transform 0.4s ease-out;
    gap: 0;
    justify-content: center;
    &.isdragging .quotes__slide {
      cursor: grabbing;
    }
  }

  &__slide {
    flex: 0 0 640px;
    -webkit-flex: 0 0 640px;
    -ms-flex: 0 0 640px;
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    gap: 24px;
    cursor: grab;
    position: relative;

    &-video {
      width: 100%;
      aspect-ratio: 16 / 9;
      border-width: 0;
      border-radius: 8px;
      -webkit-border-radius: 8px;
      -moz-border-radius: 8px;
      transition: transform 0.4s ease-in-out;
      &--zoom-out {
        -ms-transform: scale(0.765);
        -webkit-transform: scale(0.765);
        transform: scale(0.765);
      }
    }
    &-thumbnail-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-size: cover;
    }
    &-title {
      font-size: 14px;
      font-style: normal;
      font-weight: 600;
      line-height: 20px;
      letter-spacing: 0.25px;
      margin: 0;
      span {
        color: var(--primary-orange);
        display: block;
      }
    }
    &-text {
      font-size: 24px;
      font-style: normal;
      font-weight: 600;
      line-height: 32px;
      letter-spacing: 0.25px;
      max-width: 470px;
      margin: 0 16px;
    }
  }
  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 0.3s;
  }
  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }
  &__play-icon {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 50px;
    height: 50px;
    background-color: rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    transform: translate(-50%, -50%);
    cursor: pointer;
  }

  &__play-arrow {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-left: 15px solid white;
    border-top: 10px solid transparent;
    border-bottom: 10px solid transparent;
    transform: translate(-40%, -50%);
  }
  &__arrows {
    display: flex;
    gap: 18px;
    touch-action: manipulation;
    -webkit-touch-action: manipulation;
    -ms-touch-action: manipulation;
  }
  &__arrow {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid rgba(203, 203, 203, 0.24);
    background-color: #fff;
    cursor: pointer;
    &:hover {
      border: 2px solid #585858;
    }

    &--left,
    &--right {
      &::before {
        content: '';
        display: block;
        width: 24px;
        height: 24px;
        background-image: url('/static/icons/Arrow--right.svg');
        background-size: contain;
        background-repeat: no-repeat;
      }
    }

    &--left {
      &::before {
        -webkit-transform: scaleX(-1);
        -moz-transform: scaleX(-1);
        -ms-transform: scaleX(-1);
        -o-transform: scaleX(-1);
        transform: scaleX(-1);
      }
    }
  }
  &__button {
    padding: 14px 32px;
    border-radius: 32px;
    background-color: var(--primary-orange);
    color: var(--default-neutral-white);
    font-size: 16px;
    font-style: normal;
    font-weight: 600;
    line-height: 20px;
    letter-spacing: 0.75px;
    text-transform: uppercase;
    border: 2px solid var(--primary-orange);
    cursor: pointer;
    &:hover {
      background-color: var(--default-neutral-white);
      color: var(--basic-dark);
    }
  }
}
.disabled {
  opacity: 0.5;
  pointer-events: none;
}
@media only screen and (max-width: $small) {
  .quotes {
    margin: 32px auto 64px;
    gap: 32px;
    &__slide {
      flex: 0 0 323px;
      iframe {
        width: 323px;
        height: 181px;
      }
      &-text {
        font-size: 20px;
        line-height: 28px;
      }
    }
    &__arrows {
      order: 1;
    }
    &__button {
      display: none;
      margin: 32px 16px 0;
      order: 2;
    }
  }
}
</style>

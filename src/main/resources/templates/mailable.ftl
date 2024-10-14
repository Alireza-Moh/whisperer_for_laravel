<?php

namespace ${namespace};

use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
<#if shouldQueue>
use Illuminate\Contracts\Queue\ShouldQueue;
</#if>
use Illuminate\Queue\SerializesModels;
<#if useNewSyntax>
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
</#if>

class ${name} extends Mailable <#if useNewSyntax>implements ShouldQueue</#if>
{
    use Queueable, SerializesModels;

    public function __construct() {}

<#if useNewSyntax>
    public function envelope(): Envelope
    {
        return new Envelope(
            subject: '',
        );
    }

    public function content(): Content
    {
        return new Content(
<#if markdownView>
            markdown: '${viewName}',
<#else>
            view: '${viewName}',
</#if>
       );
    }

    public function attachments(): array
    {
        return [];
    }
<#else>
    public function build(): self
    {
<#if markdownView>
        return $this->markdown('${viewName}');
<#else>
        return $this->view('${viewName}');
</#if>
    }
</#if>
}
